package com.agribank.qldv_api.workflow;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.WorkflowClient;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.enums.Constants;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.request.casemgmt.WorkflowTransitionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lõi Workflow Engine dùng chung cho module Tổ chức Đảng — KHÔNG public API riêng. Endpoint
 * public duy nhất (POST /cases/{id}/workflow-action, 4 action dùng chung) gọi hàm transition()
 * ở đây; các endpoint nghiệp vụ riêng (SC-04/05/06, Sprint 3) sẽ GỌI TRỰC TIẾP hàm này với
 * action_code đặc biệt (VD UPLOAD_BTV_RESULT, APPROVE_ISSUE...) sau khi tự xử lý xong field
 * riêng của bước đó — không đi qua endpoint public.
 *
 * Engine không tự resolve user/role hiện tại (không phụ thuộc SecurityContext) — actorRoleCodes
 * và performedBy do caller truyền vào, để tái sử dụng được từ bất kỳ ngữ cảnh gọi nào.
 */
@Service
@RequiredArgsConstructor
public class WorkflowEngine {
    private final CaseClient caseClient;
    private final WorkflowClient workflowClient;

    public void transition(String caseId, String actionCode, List<String> actorRoleCodes,
                            String performedBy, String note) {
        Case caseEntity = caseClient.findById(caseId).getData().orElse(null);
        if (caseEntity == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }

        String currentStatus = caseEntity.getStatusId();
        String flowCode = caseEntity.getOriginFlow();

        WorkflowTransitionRule rule = CaseWorkflowConfig.RULES.stream()
                .filter(r -> r.flowCode().equals(flowCode)
                        && r.fromStatusCode().equals(currentStatus)
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

        if (ECaseWorkflowAction.RETURN.name().equals(actionCode) && (note == null || note.isBlank())) {
            throw new ValidationException("ERR-SC03-02: Bắt buộc nhập lý do (comment) khi chuyển trả hồ sơ");
        }

        // origin_flow KHÔNG đổi ở bất kỳ transition nào (kể cả RECEIVE_ROUTE, đã chốt) — không
        // gửi/ghi đè field này ở đây.
        WorkflowTransitionRequest request = new WorkflowTransitionRequest();
        request.setEntityTable(Constants.ENTITY_TABLE_CASE);
        request.setEntityId(caseId);
        request.setFromStatusId(currentStatus);
        request.setAction(actionCode);
        request.setToStatusId(rule.toStatusCode());
        request.setPerformedBy(performedBy);
        request.setPerformedRoleId(matchedRole);
        request.setNote(note);

        workflowClient.applyTransition(request);
    }
}
