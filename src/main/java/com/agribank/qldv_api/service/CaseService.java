package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseHistoryClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.CaseSearchRequest;
import com.agribank.qldv_api.request.casemgmt.WorkflowActionRequest;
import com.agribank.qldv_api.response.casemgmt.CaseDetailResponse;
import com.agribank.qldv_api.workflow.WorkflowEngine;
import com.agribank.qldvutils.request.casemgmt.CaseSearchQuery;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseClient caseClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final CaseHistoryClient caseHistoryClient;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final WorkflowEngine workflowEngine;
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
                userRequested.getId(), request.getComment());
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
