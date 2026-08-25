package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseHistoryClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.NotificationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.CaseSearchRequest;
import com.agribank.qldv_api.request.casemgmt.WorkflowActionRequest;
import com.agribank.qldv_api.response.casemgmt.CaseDetailResponse;
import com.agribank.qldv_api.response.casemgmt.EligibleAssigneeResponse;
import com.agribank.qldv_api.response.casemgmt.EligibleAssigneesResponse;
import com.agribank.qldv_api.workflow.WorkflowEngine;
import com.agribank.qldv_api.workflow.WorkflowRoleResolver;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.enums.ENotificationType;
import com.agribank.qldvutils.entity.NotificationRecipient;
import com.agribank.qldvutils.request.casemgmt.CaseSearchQuery;
import com.agribank.qldvutils.request.notification.NotificationPersistRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import com.agribank.qldvutils.response.notification.NotificationItemResponse;
import com.agribank.qldvutils.response.notification.NotificationPersistResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseClient caseClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final CaseHistoryClient caseHistoryClient;
    private final CaseTypeClient caseTypeClient;
    private final NotificationClient notificationClient;
    private final NotificationPushService notificationPushService;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final WorkflowEngine workflowEngine;
    private final WorkflowRoleResolver workflowRoleResolver;
    private final EligibleAssigneeService eligibleAssigneeService;
    private final ModelMapper modelMapper;

    /**
     * Phạm vi dữ liệu (RBAC) là logic nghiệp vụ, xử lý ở đây (qldv-api) — qldv-db chỉ chạy đúng
     * câu query nhận được, không tự quyết định "rỗng nghĩa là gì". allowedOrganizationIds rỗng
     * (khác null) nghĩa là user phạm vi cấp cơ sở không có tổ chức nào trong phạm vi => trả 0
     * kết quả ngay, không gọi xuống qldv-db.
     */
    public PageResponse<CaseListItemResponse> search(CaseSearchRequest request) {
        OrganizationScope scope = organizationService.resolveScope();

        if (!scope.isFull() && scope.getAllowedIds().isEmpty()) {
            return emptyPage(request);
        }

        CaseSearchQuery query = new CaseSearchQuery();
        query.setKeyword(request.getKeyword());
        query.setCaseTypeId(request.getCaseTypeId());
        query.setStatusId(request.getStatusId());
        query.setAuthorityLevel(request.getAuthorityLevel());
        query.setCreatedFrom(startOfDay(request.getCreatedFrom()));
        query.setCreatedTo(exclusiveEndOfDay(request.getCreatedTo()));
        query.setCompletedFrom(startOfDay(request.getCompletedFrom()));
        query.setCompletedTo(exclusiveEndOfDay(request.getCompletedTo()));
        query.setPage(request.getPage());
        query.setPageSize(request.getPageSize());
        query.setAllowedOrganizationIds(scope.isFull() ? null : List.copyOf(scope.getAllowedIds()));

        return caseClient.search(query).getData();
    }

    public CaseDetailResponse getById(String id) {
        OrganizationScope scope = organizationService.resolveScope();
        List<CaseOrganizationSummaryResponse> orgLinks =
                safeList(caseOrganizationClient.findWithOrganizationByCaseId(id).getData());
        checkScope(scope, orgLinks);

        CaseListItemResponse base = caseClient.findDetailById(id).getData().orElse(null);
        if (base == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }

        CaseDetailResponse response = modelMapper.map(base, CaseDetailResponse.class);
        response.setOrganizations(orgLinks);
        return response;
    }

    public List<CaseHistoryItemResponse> getHistory(String id) {
        OrganizationScope scope = organizationService.resolveScope();
        List<CaseOrganizationSummaryResponse> orgLinks =
                safeList(caseOrganizationClient.findWithOrganizationByCaseId(id).getData());
        checkScope(scope, orgLinks);

        boolean exists = caseClient.findById(id).getData().isPresent();
        if (!exists) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }

        return safeList(caseHistoryClient.findByCaseId(id).getData());
    }

    /**
     * Endpoint dùng chung cho 4 action (SUBMIT_CONTROL/RETURN/APPROVE_FORWARD/APPROVE) —
     * WorkflowActionRequest.validate() đã chặn action khác trước khi tới đây. Không kiểm tra
     * phạm vi tổ chức đảng ở đây — guard hợp lệ của bước này chỉ gồm rule khớp (flow/status/
     * action) + role khớp (WorkflowEngine PHẦN 2), đúng như spec, không tự thêm kiểm tra khác.
     */
    public void performWorkflowAction(String caseId, WorkflowActionRequest request) {
        UserDetailsImpl userRequested = userService.getUserRequested();
        if (userRequested == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        workflowEngine.transition(caseId, request.getAction(), userRequested.getRoleCodes(),
                userRequested.getId(), request.getComment(), request.getAssignedUserId());
        notifyAfterWorkflowAction(caseId, request.getAction(), request.getComment());
    }

    /**
     * Tạo PMDV_NOTIFICATION sau khi transition thành công — người xử lý bước kế tiếp (mọi action)
     * hoặc người vừa bị trả hồ sơ (RETURN). Đọc lại Case SAU transition vì WorkflowEngine đã tự
     * resolve/ghi assignedUserId mới vào đó (xem WorkflowEngine#resolveNewAssigneeId) — không tính
     * lại logic gán người ở đây, chỉ suy ra thêm nhóm broadcast khi chưa gán được 1 người cụ thể.
     * Lỗi ở bước này KHÔNG được làm hỏng transition chính (đã persist xong) — chỉ log warn.
     */
    private void notifyAfterWorkflowAction(String caseId, String action, String comment) {
        try {
            Case updatedCase = caseClient.findById(caseId).getData().orElse(null);
            if (updatedCase == null) {
                return;
            }
            List<String> recipientUserIds = resolveNotificationRecipients(updatedCase, action);
            if (recipientUserIds.isEmpty()) {
                return;
            }
            boolean isReturn = ECaseWorkflowAction.RETURN.name().equals(action);

            NotificationPersistRequest notification = new NotificationPersistRequest();
            notification.setRefId(caseId);
            notification.setType((isReturn ? ENotificationType.CASE_RETURNED : ENotificationType.TASK_ASSIGNED).name());
            notification.setTitle(buildNotificationTitle(updatedCase, isReturn));
            notification.setContent(buildNotificationContent(updatedCase, isReturn, comment));
            notification.setScreen(resolveScreen(updatedCase));
            notification.setRecipientUserIds(recipientUserIds);

            NotificationPersistResult result = notificationClient.persist(notification).getData();
            pushToRecipients(result);
        } catch (Exception e) {
            log.warn("Tạo thông báo sau workflow-action thất bại (case {}, action {}): {}", caseId, action, e.getMessage(), e);
        }
    }

    /** Đẩy real-time qua WebSocket cho từng recipient vừa lưu (đã có id thật, dùng để FE gọi API đánh dấu đã xem) — xem NotificationPushService. */
    private void pushToRecipients(NotificationPersistResult result) {
        if (result == null || result.getNotification() == null) {
            return;
        }
        for (NotificationRecipient recipient : safeList(result.getRecipients())) {
            NotificationItemResponse payload = new NotificationItemResponse(
                    recipient.getId(),
                    result.getNotification().getRefId(),
                    result.getNotification().getTitle(),
                    result.getNotification().getContent(),
                    result.getNotification().getType(),
                    result.getNotification().getScreen(),
                    recipient.getIsSeen(),
                    result.getNotification().getCreatedAt());
            notificationPushService.push(recipient.getUserId(), payload);
        }
    }

    /**
     * assignedUserId đã resolve được 1 người cụ thể (kể cả RETURN, xem WorkflowEngine) -> đúng
     * người đó. Còn lại (role bước mới thuộc nhóm full-scope, chưa gán ai cụ thể) -> broadcast cho
     * TOÀN BỘ user đủ điều kiện của role đó (EligibleAssigneeService, giống GET
     * /cases/{caseId}/eligible-assignees) — CHỈ áp dụng cho action tiến (KHÔNG áp dụng cho RETURN,
     * RETURN luôn nhắm đúng 1 người hoặc không ai, không có khái niệm "nhóm" cần trả lại).
     */
    private List<String> resolveNotificationRecipients(Case updatedCase, String action) {
        if (updatedCase.getAssignedUserId() != null) {
            return List.of(updatedCase.getAssignedUserId());
        }
        if (ECaseWorkflowAction.RETURN.name().equals(action)) {
            return List.of();
        }
        String requiredRole = workflowRoleResolver.resolveRequiredRoleForNextActor(updatedCase.getStatusId(), null);
        List<String> caseOrganizationIds = safeList(
                caseOrganizationClient.findWithOrganizationByCaseId(updatedCase.getId()).getData())
                .stream().map(CaseOrganizationSummaryResponse::getOrganizationId).toList();
        return eligibleAssigneeService.findEligibleAssignees(requiredRole, caseOrganizationIds).stream()
                .map(EligibleAssigneeResponse::getUserId)
                .toList();
    }

    /** screen của Notification = CaseType.code (ESTABLISH/DISSOLVE/MERGE/...) — cho FE biết điều hướng về màn nào khi bấm vào thông báo. Không tìm thấy case type -> null (không chặn tạo thông báo). */
    private String resolveScreen(Case updatedCase) {
        return caseTypeClient.findById(updatedCase.getCaseTypeId()).getData()
                .map(CaseType::getCode)
                .orElse(null);
    }

    private String buildNotificationTitle(Case updatedCase, boolean isReturn) {
        return isReturn
                ? "Hồ sơ " + updatedCase.getCaseCode() + " bị trả lại"
                : "Hồ sơ " + updatedCase.getCaseCode() + " cần bạn xử lý";
    }

    private String buildNotificationContent(Case updatedCase, boolean isReturn, String comment) {
        String orgName = updatedCase.getProposedOrganizationName();
        String suffix = (orgName == null || orgName.isBlank()) ? "" : " (" + orgName + ")";
        if (isReturn) {
            String reason = (comment == null || comment.isBlank()) ? "" : ": " + comment;
            return "Hồ sơ " + updatedCase.getCaseCode() + suffix + " đã bị trả lại" + reason;
        }
        return "Hồ sơ " + updatedCase.getCaseCode() + suffix + " đang chờ bạn xử lý ở bước tiếp theo";
    }

    /**
     * GET /cases/{caseId}/eligible-assignees — dùng ĐÚNG WorkflowRoleResolver (Phần 2) để suy ra
     * role cần gán tiếp theo từ (currentStatus, action), rồi tìm user đủ điều kiện qua
     * EligibleAssigneeService (Phần 3). requiredRole = null (bước không cần gán tiếp, VD trạng
     * thái FINAL/không xác định được transition) -> trả danh sách rỗng + message giải thích.
     */
    public EligibleAssigneesResponse getEligibleAssignees(String caseId, String action) {
        Case existingCase = caseClient.findById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));

        String requiredRole = workflowRoleResolver.resolveRequiredRoleForNextActor(existingCase.getStatusId(), action);
        if (requiredRole == null) {
            return new EligibleAssigneesResponse(List.of(), "Bước này không cần gán người xử lý tiếp theo");
        }

        List<String> caseOrganizationIds = safeList(caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData())
                .stream().map(CaseOrganizationSummaryResponse::getOrganizationId).toList();
        List<EligibleAssigneeResponse> assignees = eligibleAssigneeService.findEligibleAssignees(requiredRole, caseOrganizationIds);

        return new EligibleAssigneesResponse(assignees, null);
    }

    /**
     * Phạm vi dữ liệu: 1 hồ sơ thuộc phạm vi nếu CÓ ÍT NHẤT 1 dòng PMDV_CASE_ORGANIZATION của
     * hồ sơ đó trỏ tới 1 tổ chức trong tập hợp phạm vi của user (S1-05). Kiểm tra TRƯỚC khi xác
     * nhận hồ sơ có tồn tại hay không — ngoài phạm vi luôn trả 403, không trả 404.
     */
    private void checkScope(OrganizationScope scope, List<CaseOrganizationSummaryResponse> orgLinks) {
        if (scope.isFull()) {
            return;
        }
        boolean allowed = orgLinks.stream().anyMatch(link -> scope.isAllowed(link.getOrganizationId()));
        if (!allowed) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền truy cập hồ sơ này");
        }
    }

    private PageResponse<CaseListItemResponse> emptyPage(CaseSearchRequest request) {
        PageResponse<CaseListItemResponse> response = new PageResponse<>();
        response.setData(List.of());
        response.setTotalPages(0);
        response.setTotalItems(0L);
        response.setCurrentPage(request.getPage() == null ? 0 : request.getPage());
        return response;
    }

    private static Timestamp startOfDay(LocalDate date) {
        return date == null ? null : Timestamp.valueOf(date.atStartOfDay());
    }

    private static Timestamp exclusiveEndOfDay(LocalDate date) {
        return date == null ? null : Timestamp.valueOf(date.plusDays(1).atStartOfDay());
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
