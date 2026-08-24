package com.agribank.qldv_api.workflow;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseHistoryClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.WorkflowClient;
import com.agribank.qldv_api.response.casemgmt.EligibleAssigneeResponse;
import com.agribank.qldv_api.service.EligibleAssigneeService;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.enums.Constants;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.request.casemgmt.WorkflowTransitionRequest;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Lõi Workflow Engine dùng chung cho module Tổ chức Đảng — KHÔNG public API riêng. Endpoint
 * public duy nhất (POST /cases/{id}/workflow-action, 4 action dùng chung) gọi hàm transition()
 * ở đây; các endpoint nghiệp vụ riêng (SC-04/05/06, SubmitToParentService...) GỌI TRỰC TIẾP hàm
 * này với action_code đặc biệt sau khi tự xử lý xong field riêng của bước đó.
 *
 * Engine không tự resolve user/role hiện tại (không phụ thuộc SecurityContext) — actorRoleCodes
 * và performedBy do caller truyền vào, để tái sử dụng được từ bất kỳ ngữ cảnh gọi nào.
 *
 * Nguồn quản lý assignedUserId (task assigned-user): SAU khi rule/role hợp lệ, TRƯỚC khi gửi
 * transition xuống qldv-db, engine tự tính assignedUserId MỚI theo ĐÚNG action vừa thực hiện —
 * đây là ĐIỂM CHỐT DUY NHẤT mọi transition đều đi qua (kể cả action đặc biệt), nên đặt logic ở
 * đây thay vì lặp lại tại từng service gọi vào. Ngoại lệ: APPROVE_COMPLETE (CaseCompleteService)
 * KHÔNG đi qua engine này (đã bypass để atomic với việc tạo PMDV_ORGANIZATION, xem javadoc lớp
 * đó) — tự tính assignedUserId riêng, dùng lại {@link WorkflowRoleResolver} trực tiếp.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngine {
    private final CaseClient caseClient;
    private final WorkflowClient workflowClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final CaseHistoryClient caseHistoryClient;
    private final WorkflowRoleResolver workflowRoleResolver;
    private final EligibleAssigneeService eligibleAssigneeService;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;

    /**
     * {@code requestedAssigneeId}: CHỈ có ý nghĩa với action SUBMIT_CONTROL/APPROVE_FORWARD (client
     * chọn người xử lý tiếp theo qua GET /cases/{caseId}/eligible-assignees) — mọi action khác
     * truyền {@code null}, engine tự suy ra assignedUserId mới (xem {@link #resolveNewAssigneeId}).
     */
    public void transition(String caseId, String actionCode, List<String> actorRoleCodes,
                            String performedBy, String note, String requestedAssigneeId) {
        Case caseEntity = caseClient.findById(caseId).getData().orElse(null);
        if (caseEntity == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }

        String currentStatus = caseEntity.getStatusId();
        String flowCode = caseEntity.getOriginFlow();

        WorkflowTransitionRule rule = CaseWorkflowConfig.RULES.stream()
                .filter(r -> r.flowCode().equals(flowCode)
                        && r.fromStatusCode().getCode().equals(currentStatus)
                        && r.actionCode().equals(actionCode))
                .findFirst()
                .orElse(null);
        if (rule == null) {
            throw new ForbiddenException("ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại");
        }

        String matchedRole = actorRoleCodes == null ? null : actorRoleCodes.stream()
                .filter(rule.requiredRoleCodes()::contains)
                .findFirst()
                .orElse(null);
        if (matchedRole == null) {
            throw new ForbiddenException("ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại");
        }

        // Guard BỔ SUNG (task assigned-user) — chạy SAU guard vai trò/trạng thái ở trên.
        workflowAssigneeGuard.requireAssignee(caseEntity, performedBy);

        if (ECaseWorkflowAction.RETURN.name().equals(actionCode) && (note == null || note.isBlank())) {
            throw new ValidationException("ERR-SC03-02: Bắt buộc nhập lý do (comment) khi chuyển trả hồ sơ");
        }

        String newAssignedUserId = resolveNewAssigneeId(caseId, currentStatus, actionCode,
                rule.toStatusCode().getCode(), requestedAssigneeId);

        // origin_flow KHÔNG đổi ở bất kỳ transition nào (kể cả RECEIVE_ROUTE, đã chốt) — không
        // gửi/ghi đè field này ở đây.
        WorkflowTransitionRequest request = new WorkflowTransitionRequest();
        request.setEntityTable(Constants.ENTITY_TABLE_CASE);
        request.setEntityId(caseId);
        request.setFromStatusId(currentStatus);
        request.setAction(actionCode);
        request.setToStatusId(rule.toStatusCode().getCode());
        request.setPerformedBy(performedBy);
        request.setPerformedRoleId(matchedRole);
        request.setNote(note);
        request.setAssignedUserId(newAssignedUserId);

        workflowClient.applyTransition(request);
    }

    /**
     * (a) SUBMIT_CONTROL/APPROVE_FORWARD: bắt buộc nhận từ caller, PHẢI nằm trong danh sách hợp lệ
     * (role đúng bước kế tiếp + đúng phạm vi) — tính lại từ đầu ở đây, KHÔNG chỉ tin caller.
     * (b) RETURN: tra CaseHistory gần nhất (SUBMIT_CONTROL/APPROVE_FORWARD dẫn TỚI đúng
     * currentStatus) -> gán lại performedBy dòng đó; không tìm thấy -> null + log WARN.
     * (c) Còn lại: resolve role của trạng thái MỚI; role null hoặc thuộc nhóm cấp Agribank (full
     * scope) -> null; role cấp cơ sở với ĐÚNG 1 user khả dĩ trong phạm vi -> gán user đó; nhiều
     * hơn 1 -> null (chờ cơ chế gán riêng, NGOÀI PHẠM VI task này).
     */
    private String resolveNewAssigneeId(String caseId, String currentStatus, String actionCode,
                                         String newStatus, String requestedAssigneeId) {
        boolean isAssignableByCaller = ECaseWorkflowAction.SUBMIT_CONTROL.name().equals(actionCode)
                || ECaseWorkflowAction.APPROVE_FORWARD.name().equals(actionCode);
        if (isAssignableByCaller) {
            return resolveCallerAssignedId(caseId, newStatus, requestedAssigneeId);
        }

        if (ECaseWorkflowAction.RETURN.name().equals(actionCode)) {
            return resolveReturnAssigneeId(caseId, currentStatus);
        }

        return resolveAutoAssigneeId(caseId, newStatus);
    }

    private String resolveCallerAssignedId(String caseId, String newStatus, String requestedAssigneeId) {
        if (requestedAssigneeId == null || requestedAssigneeId.isBlank()) {
            throw new ValidationException("assignedUserId không được để trống với action này");
        }

        String requiredRole = workflowRoleResolver.resolveRequiredRoleForNextActor(newStatus, null);
        List<EligibleAssigneeResponse> eligible = eligibleAssigneeService.findEligibleAssignees(
                requiredRole, caseOrganizationIds(caseId));
        boolean valid = eligible.stream().anyMatch(a -> a.getUserId().equals(requestedAssigneeId));

        if (!valid) {
            throw new ForbiddenException("Người được chọn không có quyền xử lý bước tiếp theo của hồ sơ này");
        }

        return requestedAssigneeId;
    }

    private String resolveReturnAssigneeId(String caseId, String currentStatus) {
        List<CaseHistoryItemResponse> history = safeList(caseHistoryClient.findByCaseId(caseId).getData());
        CaseHistoryItemResponse lastSubmit = history.stream()
                .filter(h -> currentStatus.equals(h.getToStatusId())
                        && (ECaseWorkflowAction.SUBMIT_CONTROL.name().equals(h.getAction())
                            || ECaseWorkflowAction.APPROVE_FORWARD.name().equals(h.getAction())))
                .max(Comparator.comparing(CaseHistoryItemResponse::getProcessedAt))
                .orElse(null);
        if (lastSubmit == null) {
            log.warn("RETURN case {}: không tìm thấy dòng CaseHistory SUBMIT_CONTROL/APPROVE_FORWARD dẫn tới {} "
                    + "— set assignedUserId = null", caseId, currentStatus);
            return null;
        }
        return lastSubmit.getPerformedBy();
    }

    private String resolveAutoAssigneeId(String caseId, String newStatus) {
        String requiredRole = workflowRoleResolver.resolveRequiredRoleForNextActor(newStatus, null);
        if (requiredRole == null || eligibleAssigneeService.isFullScopeRole(requiredRole)) {
            return null;
        }

        List<EligibleAssigneeResponse> eligible = eligibleAssigneeService.findEligibleAssignees(
                requiredRole, caseOrganizationIds(caseId));
        return eligible.size() == 1 ? eligible.get(0).getUserId() : null;
    }

    private List<String> caseOrganizationIds(String caseId) {
        List<CaseOrganizationSummaryResponse> links = safeList(
                caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());

        return links.stream().map(CaseOrganizationSummaryResponse::getOrganizationId).toList();
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
