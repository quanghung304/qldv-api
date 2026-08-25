package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.response.casemgmt.EligibleAssigneeResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.enums.ERoleCode;
import com.agribank.qldvutils.request.SearchUserRequest;
import com.agribank.qldvutils.response.user.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tìm user ĐỦ ĐIỀU KIỆN được gán {@code assignedUserId} cho 1 role cụ thể — dùng chung cho
 * GET /cases/{caseId}/eligible-assignees (Phần 3) VÀ cơ chế tự động gán sau transition của
 * {@link com.agribank.qldv_api.workflow.WorkflowEngine} (Phần 4 mục 3c).
 *
 * Phân loại "cấp Agribank" (full scope) / "cấp cơ sở" (org scope) dùng ĐÚNG 2 nhóm role đã có ở
 * {@code OrganizationService.FULL_SCOPE_ROLES}/{@code ORG_SCOPE_ROLES} (không đổi tên/giá trị,
 * chỉ khai lại cục bộ vì 2 field đó đang {@code private} — cùng convention chấp nhận trùng lặp nhỏ
 * đã thấy ở {@code CaseFlowRoleGuard}/{@code CaseDeleteService}), thu hẹp lại đúng các role THỰC
 * SỰ xuất hiện trong {@code CaseWorkflowConfig.RULES} (bỏ R-ADM/R-QTVCS — không tham gia luồng).
 */
@Service
@RequiredArgsConstructor
public class EligibleAssigneeService {
    private static final Set<String> FULL_SCOPE_ROLES = Set.of(
            ERoleCode.R_CV.getCode(), ERoleCode.R_KS.getCode(), ERoleCode.R_LD.getCode());
    private static final Set<String> ORG_SCOPE_ROLES = Set.of(
            ERoleCode.R_BPTM.getCode(), ERoleCode.R_KSCS.getCode(), ERoleCode.R_PDCS.getCode());
    private static final int MAX_ANCESTOR_HOPS = 20;
    private static final int SEARCH_PAGE_SIZE = 500;

    private final UserClient userClient;
    private final OrganizationClient organizationClient;

    public boolean isOrgScopeRole(String roleCode) {
        return ORG_SCOPE_ROLES.contains(roleCode);
    }

    public boolean isFullScopeRole(String roleCode) {
        return FULL_SCOPE_ROLES.contains(roleCode);
    }

    /**
     * requiredRole thuộc {@link #FULL_SCOPE_ROLES} (R-CV/R-KS/R-LD): trả toàn bộ user có role đó,
     * không lọc thêm. requiredRole thuộc {@link #ORG_SCOPE_ROLES} (R-BPTM/R-KSCS/R-PDCS): lọc
     * user có PMDV_STAFF.organization_id khớp CHÍNH tổ chức gắn với hồ sơ, HOẶC khớp 1 tổ tiên của
     * tổ chức đó (đúng hướng "user cấp trên phụ trách luôn hồ sơ của chi bộ trực thuộc", xem
     * workflow-states.md) — merge + dedupe theo userId. requiredRole khác 2 nhóm trên (không tham
     * gia luồng phê duyệt) -> trả rỗng.
     */
    public List<EligibleAssigneeResponse> findEligibleAssignees(String requiredRole, List<String> caseOrganizationIds) {
        if (requiredRole == null) {
            return List.of();
        }
        if (isFullScopeRole(requiredRole)) {
            return searchByRoleAndOrganization(requiredRole, null);
        }
        if (isOrgScopeRole(requiredRole)) {
            Set<String> candidateOrgIds = resolveAncestorOrganizationIds(caseOrganizationIds);
            Map<String, EligibleAssigneeResponse> byUserId = new LinkedHashMap<>();
            for (String orgId : candidateOrgIds) {
                for (EligibleAssigneeResponse candidate : searchByRoleAndOrganization(requiredRole, orgId)) {
                    byUserId.putIfAbsent(candidate.getUserId(), candidate);
                }
            }
            return List.copyOf(byUserId.values());
        }
        return List.of();
    }

    private List<EligibleAssigneeResponse> searchByRoleAndOrganization(String roleCode, String organizationId) {
        SearchUserRequest request = new SearchUserRequest();
        request.setRoleId(roleCode);
        request.setOrganizationId(organizationId);
        request.setDelete(0);
        request.setPage(0);
        request.setPageSize(SEARCH_PAGE_SIZE);

        List<UserSearchResponse> users = safeList(userClient.search(request).getData().getData());
        return users.stream()
                .map(u -> new EligibleAssigneeResponse(u.getId(), u.getFullName(), roleCode))
                .toList();
    }

    /**
     * Tập hợp organizationId của chính các tổ chức gắn với hồ sơ, cộng dồn TỪNG tổ tiên (qua
     * parent_organization_id) của mỗi tổ chức đó tới gốc — walk trực tiếp qua OrganizationClient
     * (hierarchy nông, vài cấp), giới hạn {@link #MAX_ANCESTOR_HOPS} để tránh vòng lặp vô hạn nếu
     * dữ liệu tổ chức bị lỗi (cha trỏ vòng).
     */
    private Set<String> resolveAncestorOrganizationIds(List<String> caseOrganizationIds) {
        Set<String> result = new LinkedHashSet<>();
        if (caseOrganizationIds == null) {
            return result;
        }
        for (String orgId : caseOrganizationIds) {
            String currentId = orgId;
            int hops = 0;
            while (currentId != null && result.add(currentId) && hops < MAX_ANCESTOR_HOPS) {
                Organization organization = organizationClient.findById(currentId).getData().orElse(null);
                currentId = organization == null ? null : organization.getParentOrganizationId();
                hops++;
            }
        }
        return result;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
