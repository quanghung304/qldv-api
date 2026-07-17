package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CommitteeMemberClient;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.response.category.OrganizationTypeResponse;
import com.agribank.qldv_api.response.organization.OrganizationDetailResponse;
import com.agribank.qldv_api.response.organization.OrganizationListItemResponse;
import com.agribank.qldv_api.response.organization.ParentOrganizationResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.request.organization.OrganizationSearchQuery;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.organization.CommitteeMemberResponse;
import com.agribank.qldvutils.response.organization.OrganizationChildCountResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private static final Set<String> FULL_SCOPE_ROLES = Set.of("R-ADM", "R-CV", "R-KS", "R-LD");
    private static final Set<String> ORG_SCOPE_ROLES = Set.of("R-QTVCS", "R-BPTM", "R-PDCS", "R-KSCS");
    private static final String ROLE_DVTT = "R-DVTT";
    private static final int STATUS_OFFICIAL = 2;

    private final OrganizationClient organizationClient;
    private final OrganizationTypeClient organizationTypeClient;
    private final CommitteeMemberClient committeeMemberClient;
    private final IAMClient iamClient;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public PageResponse<OrganizationListItemResponse> search(OrganizationSearchRequest request) {
        OrganizationScope scope = resolveScope();

        OrganizationSearchQuery query = new OrganizationSearchQuery();
        query.setKeyword(request.getKeyword());
        query.setStatus(request.getStatus());
        query.setOrganizationTypeId(request.getOrganizationTypeId());
        query.setDecisionDateFrom(request.getDecisionDateFrom());
        query.setDecisionDateTo(request.getDecisionDateTo());
        query.setPage(request.getPage());
        query.setPageSize(request.getPageSize());
        query.setSort(request.getSort());
        query.setOrderBy(request.getOrderBy());
        query.setAllowedIds(scope.isFull() ? null : List.copyOf(scope.getAllowedIds()));

        PageResponse<Organization> page = organizationClient.search(query).getData();

        List<Organization> items = page.getData() == null ? List.of() : page.getData();
        Map<String, OrganizationType> typeMap = fetchTypeMap();
        Map<Integer, String> branchNameMap = fetchBranchNames(items.stream().map(Organization::getBrcd).toList());
        Map<String, Long> childCountByParent = fetchChildCounts(items.stream().map(Organization::getId).toList());

        List<OrganizationListItemResponse> responseItems = items.stream()
                .map(o -> toListItem(o, typeMap, branchNameMap, childCountByParent))
                .toList();

        PageResponse<OrganizationListItemResponse> response = new PageResponse<>();
        response.setData(responseItems);
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalItems());
        response.setCurrentPage(page.getCurrentPage());
        return response;
    }

    public OrganizationDetailResponse getById(String id) {
        OrganizationScope scope = resolveScope();
        if (!scope.isAllowed(id)) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền truy cập tổ chức đảng này");
        }

        Organization org = organizationClient.findById(id).getData().orElse(null);
        if (org == null) {
            throw new NotFoundException("Không tìm thấy tổ chức đảng");
        }

        Map<String, OrganizationType> typeMap = fetchTypeMap();
        Map<Integer, String> branchNameMap = fetchBranchNames(List.of(org.getBrcd()));
        Map<String, Long> childCountByParent = fetchChildCounts(List.of(org.getId()));

        OrganizationListItemResponse base = toListItem(org, typeMap, branchNameMap, childCountByParent);
        OrganizationDetailResponse response = modelMapper.map(base, OrganizationDetailResponse.class);

        if (org.getParentOrganizationId() != null) {
            Organization parent = organizationClient.findById(org.getParentOrganizationId()).getData().orElse(null);
            if (parent != null) {
                ParentOrganizationResponse parentResponse = new ParentOrganizationResponse();
                parentResponse.setId(parent.getId());
                parentResponse.setOrganizationName(parent.getOrganizationName());
                response.setParentOrganization(parentResponse);
            }
        }

        return response;
    }

    public List<CommitteeMemberResponse> getCommitteeMembers(String id) {
        OrganizationScope scope = resolveScope();
        if (!scope.isAllowed(id)) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền truy cập tổ chức đảng này");
        }

        Organization org = organizationClient.findById(id).getData().orElse(null);
        if (org == null) {
            throw new NotFoundException("Không tìm thấy tổ chức đảng");
        }

        // Join CommitteeMember + Staff ngay trong 1 câu query ở qldv-db — không cần gọi thêm
        // staffClient để lấy full_name.
        return safeList(committeeMemberClient.findByOrganizationIdAndStatus(id, STATUS_OFFICIAL).getData());
    }

    /**
     * Phạm vi dữ liệu: role_code + partyOrganizationId đọc THẲNG từ UserDetailsImpl (đã resolve
     * 1 lần lúc xác thực trong JwtTokenFilter) — không query lại PMDV_USER_ROLE / PMDV_STAFF ở
     * đây. Chỉ gọi DB đúng 1 lần để lấy tổ chức trực thuộc (findDescendantIds), khi cần org-scope.
     */
    private OrganizationScope resolveScope() {
        UserDetailsImpl userRequested = userService.getUserRequested();
        if (userRequested == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }

        List<String> roleCodes = userRequested.getRoleCodes();
        Set<String> effective = new HashSet<>(roleCodes == null ? List.of() : roleCodes);
        effective.remove(ROLE_DVTT);

        if (effective.isEmpty()) {
            throw new ForbiddenException("ERR-GL-02: Vai trò của bạn chưa được cấp phạm vi truy cập tổ chức đảng");
        }

        boolean hasFull = effective.stream().anyMatch(FULL_SCOPE_ROLES::contains);
        if (hasFull) {
            return OrganizationScope.full();
        }

        boolean allOrgScope = ORG_SCOPE_ROLES.containsAll(effective);
        if (!allOrgScope) {
            throw new ForbiddenException("ERR-GL-02: Vai trò không xác định, chưa hỗ trợ tra cứu tổ chức đảng");
        }

        String partyOrganizationId = userRequested.getPartyOrganizationId();
        if (partyOrganizationId == null || partyOrganizationId.isBlank()) {
            return OrganizationScope.restricted(Set.of());
        }

        List<String> descendantIds = safeList(organizationClient.findDescendantIds(partyOrganizationId).getData());
        return OrganizationScope.restricted(new HashSet<>(descendantIds));
    }

    private OrganizationListItemResponse toListItem(Organization o, Map<String, OrganizationType> typeMap,
                                                      Map<Integer, String> branchNameMap,
                                                      Map<String, Long> childCountByParent) {
        OrganizationListItemResponse response = new OrganizationListItemResponse();
        response.setOrganizationId(o.getId());
        response.setOrganizationCode(o.getOrganizationCode());
        response.setOrganizationName(o.getOrganizationName());

        OrganizationType type = typeMap.get(o.getOrganizationTypeId());
        if (type != null) {
            response.setOrganizationType(modelMapper.map(type, OrganizationTypeResponse.class));
        }

        response.setBrcd(o.getBrcd());
        response.setBranchName(o.getBrcd() != null ? branchNameMap.get(o.getBrcd()) : null);
        response.setIsAuthorized(o.getIsAuthorized());

        boolean isActive = Objects.equals(o.getOperationStatus(), EOperationStatus.ACTIVE.getId());
        response.setDecisionNoDisplay(isActive ? o.getEstablishDecisionNo() : o.getDissolveDecisionNo());
        response.setDecisionDateDisplay(isActive ? o.getEstablishDecisionDate() : o.getDissolveDecisionDate());

        response.setOperationStatus(o.getOperationStatus());
        response.setMemberCount(o.getMemberCount());
        response.setCommitteeMemberCount(o.getCommitteeMemberCount());
        response.setAffiliatedCellCount(childCountByParent.getOrDefault(o.getId(), 0L).intValue());
        return response;
    }

    private Map<String, OrganizationType> fetchTypeMap() {
        List<OrganizationType> types = safeList(organizationTypeClient.findAll().getData());
        Map<String, OrganizationType> map = new HashMap<>();
        for (OrganizationType type : types) {
            map.put(type.getId(), type);
        }
        return map;
    }

    private Map<String, Long> fetchChildCounts(List<String> organizationIds) {
        List<String> distinctIds = organizationIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        List<OrganizationChildCountResponse> counts = safeList(
                organizationClient.countChildrenByParentIds(distinctIds).getData());
        Map<String, Long> map = new HashMap<>();
        for (OrganizationChildCountResponse count : counts) {
            map.put(count.getParentOrganizationId(), count.getChildCount());
        }
        return map;
    }

    private Map<Integer, String> fetchBranchNames(List<Integer> brcds) {
        List<Integer> distinctBrcds = brcds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctBrcds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, String> map = new HashMap<>();
        try {
            List<BranchResponse> branches = iamClient.getBranchInfo(distinctBrcds).getData();
            if (branches != null) {
                for (BranchResponse branch : branches) {
                    map.put(branch.getBrcd(), branch.getLclbrnm());
                }
            }
        } catch (Exception e) {
            System.out.println("OrganizationService -> fetchBranchNames: " + e.getMessage());
        }
        return map;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
