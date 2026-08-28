package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseChangeClient;
import com.agribank.qldv_api.gateway.CaseChangeTargetClient;
import com.agribank.qldv_api.gateway.CaseChangeTargetCommitteeClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentCommitteeClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.GeneratedDocumentClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.casemgmt.CompleteCaseResponse;
import com.agribank.qldv_api.response.casemgmt.CompletedOrganizationResponse;
import com.agribank.qldv_api.workflow.CaseWorkflowConfig;
import com.agribank.qldv_api.workflow.WorkflowAssigneeGuard;
import com.agribank.qldv_api.workflow.WorkflowTransitionRule;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseChange;
import com.agribank.qldvutils.entity.CaseChangeTarget;
import com.agribank.qldvutils.entity.CaseChangeTargetCommittee;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.CommitteeMember;
import com.agribank.qldvutils.entity.GeneratedDocument;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.enums.ECommitteeMemberStatus;
import com.agribank.qldvutils.enums.ELinkRole;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.casemgmt.CaseCompletePersistRequest;
import com.agribank.qldvutils.request.casemgmt.NewOrganizationEntry;
import com.agribank.qldvutils.response.casemgmt.CaseCompleteResult;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API-SC06-02 (POST /cases/{id}/complete, KHÔNG có request body) mở rộng cho 6 loại nghiệp vụ
 * (API_HoanThanh_6LoaiNghiepVu.md) — Thành lập/Giải thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên. Toàn
 * bộ dữ liệu TCĐ đích (organizationTypeId/committee/memberCount, Sáp nhập/Hợp nhất/Chia tách) ĐÃ
 * được nhập từ Bước 1 (API-SC08-01/02, {@code CaseChangeTarget}/{@code CaseChangeTargetCommittee})
 * — endpoint này CHỈ đọc lại, KHÔNG nhận body, đồng nhất với Thành lập/Giải thể/Đổi tên.
 *
 * Guard chung (role khớp rule, trạng thái A-16/B-04) dùng {@link CaseWorkflowConfig#RULES} làm
 * nguồn chân lý duy nhất — sau đó rẽ nhánh theo {@code CaseType.code}, mỗi nhánh build 1
 * {@link CaseCompletePersistRequest} rồi gửi ĐÚNG 1 lệnh Feign xuống qldv-db, chạy trong 1
 * transaction ({@code CaseCompleteService}, qldv-db).
 *
 * [GC-HT6-01] {@code establish_decision_no/date} (Thành lập, Sáp nhập/Hợp nhất/Chia tách) và
 * {@code dissolve_decision_no/date} (Giải thể/Sáp nhập/Hợp nhất/Chia tách) LẼ RA phải lấy từ 1 văn
 * bản "Quyết định" ban hành riêng ở Bước 3 của case — nhưng SC-08 (5 nghiệp vụ biến động) hiện
 * KHÔNG có API tương đương {@code DecisionDocumentsService}/API-SC05-02 để nhập văn bản đó. Tạm
 * dùng {@code CaseChange.boardDecisionNo}/{@code boardDecisionDate} (đã nhập từ Bước 1, SC-08) làm
 * nguồn thay thế — ĐÂY LÀ GIẢ ĐỊNH, cần xác nhận lại với team trước khi go-live.
 * [GC-S3-03-04] A-12→A-14 (Luồng A) đi qua cụm kiểm soát A-13 bằng 2 action dùng chung
 * (SUBMIT_CONTROL/APPROVE_FORWARD, xem {@code CaseWorkflowConfig.RULES}) — KHÔNG cần mã action
 * riêng, không tự động (khác thiết kế S3-02 gốc), xem {@link ArchiveCaseService}.
 *
 * Khóa hồ sơ (BR-SC06-03) KHÔNG cần thêm cột/flag mới: mọi endpoint ghi dữ liệu Bước 1-3
 * (EstablishmentCaseService/BoardReviewService/CommitteeReviewService/DecisionDocumentsService)
 * đã tự guard đúng 1 status_id bắt buộc riêng (A-01/A-04/A-08/A-12) — A-15 không khớp bất kỳ guard
 * nào trong số đó nên tự động read-only "by construction", không cần cơ chế khóa riêng.
 * BR-SC06-04 (không cho xóa PMDV_ATTACHMENT dù Admin) hiện chưa có endpoint xóa attachment nào
 * trong dự án để áp dụng guard này — ghi nhận là gap, ngoài phạm vi 2 API của task này.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseCompleteService {
    private static final String ESTABLISH_CASE_TYPE_CODE = "ESTABLISH";
    private static final String DISSOLVE_CASE_TYPE_CODE = "DISSOLVE";
    private static final String MERGE_CASE_TYPE_CODE = "MERGE";
    private static final String CONSOLIDATE_CASE_TYPE_CODE = "CONSOLIDATE";
    private static final String SPLIT_CASE_TYPE_CODE = "SPLIT";
    private static final String RENAME_CASE_TYPE_CODE = "RENAME";
    private static final String ORGANIZATION_CODE_PREFIX = "TCD";

    private final CaseClient caseClient;
    private final CaseTypeClient caseTypeClient;
    private final CaseEstablishmentClient caseEstablishmentClient;
    private final CaseEstablishmentCommitteeClient caseEstablishmentCommitteeClient;
    private final CaseChangeClient caseChangeClient;
    private final CaseChangeTargetClient caseChangeTargetClient;
    private final CaseChangeTargetCommitteeClient caseChangeTargetCommitteeClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final GeneratedDocumentClient generatedDocumentClient;
    private final OrganizationClient organizationClient;
    private final OrganizationTypeClient organizationTypeClient;
    private final UserService userService;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;

    public CompleteCaseResponse complete(String caseId) {
        Case existingCase = requireCase(caseId);
        UserDetailsImpl user = requireUser();

        WorkflowTransitionRule rule = requireMatchingRule(existingCase);
        String matchedRole = requireMatchedRole(rule, user.getRoleCodes());
        workflowAssigneeGuard.requireAssignee(existingCase, user.getId());

        CaseType caseType = requireCaseType(existingCase.getCaseTypeId());

        CaseCompletePersistRequest persistRequest = switch (caseType.getCode()) {
            case ESTABLISH_CASE_TYPE_CODE -> buildEstablishPersistRequest(existingCase);
            case DISSOLVE_CASE_TYPE_CODE -> buildDissolvePersistRequest(existingCase);
            case MERGE_CASE_TYPE_CODE, CONSOLIDATE_CASE_TYPE_CODE -> buildMergeOrConsolidatePersistRequest(existingCase);
            case SPLIT_CASE_TYPE_CODE -> buildSplitPersistRequest(existingCase);
            case RENAME_CASE_TYPE_CODE -> buildRenamePersistRequest(existingCase);
            default -> throw new CommonException("Loại nghiệp vụ '" + caseType.getCode() + "' chưa được hỗ trợ ở API-SC06-02");
        };
        persistRequest.setCaseId(caseId);
        persistRequest.setFromStatusId(existingCase.getStatusId());
        persistRequest.setToStatusId(rule.toStatusCode().getCode());
        persistRequest.setAction(ECaseWorkflowAction.APPROVE_COMPLETE.name());
        persistRequest.setPerformedBy(user.getId());
        persistRequest.setPerformedRoleId(matchedRole);

        CaseCompletePersistRequest request = new CaseCompletePersistRequest();
        request.setCaseId(caseId);
        request.setFromStatusId(existingCase.getStatusId());
        request.setToStatusId(rule.toStatusCode().getCode());
        request.setAction(ECaseWorkflowAction.APPROVE_COMPLETE.name());
        request.setPerformedBy(user.getId());
        request.setPerformedRoleId(matchedRole);
        request.setAssignedUserId(null);
        CaseCompleteResult result = caseClient.completeCase(persistRequest).getData();
        return buildResponse(caseId, rule.toStatusCode().getCode(), result);
    }

    // ---------------------------------------------------------------- guard chung

    private Case requireCase(String caseId) {
        return caseClient.findById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private WorkflowTransitionRule requireMatchingRule(Case existingCase) {
        return CaseWorkflowConfig.RULES.stream()
                .filter(r -> r.flowCode().equals(existingCase.getOriginFlow())
                        && r.fromStatusCode().getCode().equals(existingCase.getStatusId())
                        && r.actionCode().equals(ECaseWorkflowAction.APPROVE_COMPLETE.name()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại"));
    }

    private String requireMatchedRole(WorkflowTransitionRule rule, List<String> roleCodes) {
        return (roleCodes == null ? List.<String>of() : roleCodes).stream()
                .filter(rule.requiredRoleCodes()::contains)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại"));
    }

    private CaseType requireCaseType(String caseTypeId) {
        return caseTypeClient.findById(caseTypeId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy danh mục loại nghiệp vụ (case_type_id=" + caseTypeId + ")"));
    }

    // ---------------------------------------------------------------- handleEstablish

    private CaseCompletePersistRequest buildEstablishPersistRequest(Case existingCase) {
        CaseEstablishment establishment = caseEstablishmentClient.findByCaseId(existingCase.getId()).getData()
                .orElseThrow(() -> new CommonException("Chưa có dữ liệu Bước 1 (PMDV_CASE_ESTABLISHMENT) của hồ sơ này"));
        GeneratedDocument establishDoc = requireEstablishDecisionDocument(existingCase.getId());
        String organizationName = requireOrganizationName(existingCase);

        Map<String, String> errors = new LinkedHashMap<>();
        validateOrganizationNameUnique(errors, "organizationName", organizationName);
        throwIfInvalid(errors);

        Organization organization = Organization.builder()
                .organizationCode(generateOrganizationCode())
                .organizationName(organizationName)
                .organizationTypeId(establishment.getOrganizationTypeId())
                .brcd(establishment.getBrcd())
                .memberCount(establishment.getMemberCount())
                .committeeMemberCount(establishment.getCommitteeMemberCount())
                .operationStatus(EOperationStatus.ACTIVE.getId())
                .establishDecisionNo(establishDoc.getDocumentNo())
                .establishDecisionDate(establishDoc.getDocumentDate())
                .build();

        List<CaseEstablishmentCommittee> rows = safeList(caseEstablishmentCommitteeClient.findByCaseId(existingCase.getId()).getData());
        List<CommitteeMember> committee = rows.stream()
                .map(row -> CommitteeMember.builder()
                        .staffCode(row.getStaffCode())
                        .position(row.getProposedPosition())
                        .status(ECommitteeMemberStatus.OFFICIAL.getId())
                        .build())
                .toList();

        NewOrganizationEntry entry = new NewOrganizationEntry();
        entry.setOrganization(organization);
        entry.setCommittee(committee);

        CaseCompletePersistRequest request = new CaseCompletePersistRequest();
        request.setNewOrganizations(List.of(entry));
        return request;
    }

    private GeneratedDocument requireEstablishDecisionDocument(String caseId) {
        GeneratedDocument document = generatedDocumentClient.findByCaseIdAndDocumentName(caseId, Constants.ESTABLISH_DECISION_DOCUMENT_NAME)
                .getData().orElse(null);
        if (document == null || document.getDocumentNo() == null || document.getDocumentDate() == null) {
            throw new CommonException("Chưa có đủ dữ liệu Quyết định thành lập tổ chức đảng (Bước 3) của hồ sơ này");
        }
        return document;
    }

    private String requireOrganizationName(Case existingCase) {
        String name = existingCase.getProposedOrganizationName();
        if (name == null || name.isBlank()) {
            throw new CommonException("Hồ sơ chưa có tên tổ chức đảng dự kiến");
        }
        return name;
    }

    // ---------------------------------------------------------------- handleDissolve

    private CaseCompletePersistRequest buildDissolvePersistRequest(Case existingCase) {
        List<String> sourceOrganizationIds = fetchSourceOrganizationIds(existingCase.getId());
        if (sourceOrganizationIds.isEmpty()) {
            throw new CommonException("Hồ sơ Giải thể chưa có tổ chức đảng nguồn (PMDV_CASE_ORGANIZATION)");
        }
        CaseChange caseChange = requireCaseChange(existingCase.getId());

        CaseCompletePersistRequest request = new CaseCompletePersistRequest();
        request.setSourceOrganizationIdsToDissolve(sourceOrganizationIds);
        request.setDissolveDecisionNo(caseChange.getBoardDecisionNo());
        request.setDissolveDecisionDate(caseChange.getBoardDecisionDate());
        return request;
    }

    // ---------------------------------------------------------------- handleMergeOrConsolidate

    /**
     * BR-SC08-05 dùng chữ "TCĐ MỚI" cho cả Sáp nhập lẫn Hợp nhất, hệ thống không có field phân
     * biệt 2 trường hợp — xử lý GIỐNG HỆT NHAU, luôn tạo tổ chức mới (không tái sử dụng 1 trong
     * các tổ chức nguồn dù {@code CaseChange.survivorOrganizationId} có được set ở Bước 1 hay
     * không).
     */
    private CaseCompletePersistRequest buildMergeOrConsolidatePersistRequest(Case existingCase) {
        List<CaseChangeTarget> targets = fetchTargets(existingCase.getId());
        if (targets.size() != 1) {
            throw new CommonException("Hồ sơ Sáp nhập/Hợp nhất cần đúng 1 dòng PMDV_CASE_CHANGE_TARGET (đã nhập ở Bước 1)");
        }
        CaseChangeTarget target = targets.get(0);

        Map<String, String> errors = new LinkedHashMap<>();
        validateOrganizationNameUnique(errors, "targets[0].organizationName", target.getOrganizationName());
        validateOrganizationTypesExist(errors, targets);
        throwIfInvalid(errors);

        List<String> sourceOrganizationIds = fetchSourceOrganizationIds(existingCase.getId());
        if (sourceOrganizationIds.size() < 2) {
            throw new CommonException("Hồ sơ Sáp nhập/Hợp nhất cần >= 2 tổ chức đảng nguồn");
        }
        CaseChange caseChange = requireCaseChange(existingCase.getId());

        List<CommitteeMember> committee = buildCommitteeMembers(fetchTargetCommittee(List.of(target.getId())));
        Organization organization = Organization.builder()
                .organizationCode(generateOrganizationCode())
                .organizationName(target.getOrganizationName())
                .organizationTypeId(target.getOrganizationTypeId())
                .operationStatus(EOperationStatus.ACTIVE.getId())
                .establishDecisionNo(caseChange.getBoardDecisionNo())
                .establishDecisionDate(caseChange.getBoardDecisionDate())
                .memberCount(caseChange.getAffectedMemberCount())
                .committeeMemberCount(committee.size())
                .build();

        NewOrganizationEntry entry = new NewOrganizationEntry();
        entry.setOrganization(organization);
        entry.setCommittee(committee);

        CaseCompletePersistRequest persistRequest = new CaseCompletePersistRequest();
        persistRequest.setNewOrganizations(List.of(entry));
        persistRequest.setSourceOrganizationIdsToDissolve(sourceOrganizationIds);
        persistRequest.setDissolveDecisionNo(caseChange.getBoardDecisionNo());
        persistRequest.setDissolveDecisionDate(caseChange.getBoardDecisionDate());
        return persistRequest;
    }

    // ---------------------------------------------------------------- handleSplit

    private CaseCompletePersistRequest buildSplitPersistRequest(Case existingCase) {
        List<CaseChangeTarget> targets = fetchTargets(existingCase.getId());
        if (targets.size() < 2) {
            throw new CommonException("Hồ sơ Chia tách cần >= 2 dòng PMDV_CASE_CHANGE_TARGET (đã nhập ở Bước 1)");
        }

        Map<String, String> errors = new LinkedHashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            validateOrganizationNameUnique(errors, "targets[" + i + "].organizationName", targets.get(i).getOrganizationName());
        }
        validateOrganizationTypesExist(errors, targets);
        throwIfInvalid(errors);

        List<String> sourceOrganizationIds = fetchSourceOrganizationIds(existingCase.getId());
        if (sourceOrganizationIds.size() != 1) {
            throw new CommonException("Hồ sơ Chia tách cần đúng 1 tổ chức đảng nguồn");
        }
        CaseChange caseChange = requireCaseChange(existingCase.getId());

        List<String> targetIds = targets.stream().map(CaseChangeTarget::getId).toList();
        Map<String, List<CaseChangeTargetCommittee>> committeeByTargetId = fetchTargetCommittee(targetIds).stream()
                .collect(Collectors.groupingBy(CaseChangeTargetCommittee::getCaseChangeTargetId));

        List<NewOrganizationEntry> entries = new ArrayList<>();
        for (CaseChangeTarget target : targets) {
            List<CommitteeMember> committee = buildCommitteeMembers(safeList(committeeByTargetId.get(target.getId())));
            Organization organization = Organization.builder()
                    .organizationCode(generateOrganizationCode())
                    .organizationName(target.getOrganizationName())
                    .organizationTypeId(target.getOrganizationTypeId())
                    .operationStatus(EOperationStatus.ACTIVE.getId())
                    .establishDecisionNo(caseChange.getBoardDecisionNo())
                    .establishDecisionDate(caseChange.getBoardDecisionDate())
                    .memberCount(target.getMemberCount())
                    .committeeMemberCount(committee.size())
                    .build();
            NewOrganizationEntry entry = new NewOrganizationEntry();
            entry.setOrganization(organization);
            entry.setCommittee(committee);
            entries.add(entry);
        }

        CaseCompletePersistRequest persistRequest = new CaseCompletePersistRequest();
        persistRequest.setNewOrganizations(entries);
        persistRequest.setSourceOrganizationIdsToDissolve(sourceOrganizationIds);
        persistRequest.setDissolveDecisionNo(caseChange.getBoardDecisionNo());
        persistRequest.setDissolveDecisionDate(caseChange.getBoardDecisionDate());
        return persistRequest;
    }

    // ---------------------------------------------------------------- handleRename

    private CaseCompletePersistRequest buildRenamePersistRequest(Case existingCase) {
        List<String> sourceOrganizationIds = fetchSourceOrganizationIds(existingCase.getId());
        if (sourceOrganizationIds.size() != 1) {
            throw new ForbiddenException("ERR-SC08-07: Đổi tên chỉ áp dụng cho đúng 1 tổ chức đảng");
        }
        String newName = requireOrganizationName(existingCase);

        Map<String, String> errors = new LinkedHashMap<>();
        validateOrganizationNameUnique(errors, "organizationName", newName);
        throwIfInvalid(errors);

        CaseCompletePersistRequest request = new CaseCompletePersistRequest();
        request.setRenameOrganizationId(sourceOrganizationIds.get(0));
        request.setRenameNewName(newName);
        return request;
    }

    // ---------------------------------------------------------------- helpers dùng chung

    private CaseChange requireCaseChange(String caseId) {
        return caseChangeClient.findByCaseId(caseId).getData()
                .orElseThrow(() -> new CommonException("Chưa có dữ liệu Bước 1 (PMDV_CASE_CHANGE) của hồ sơ này"));
    }

    private List<CaseChangeTarget> fetchTargets(String caseId) {
        return safeList(caseChangeTargetClient.findByCaseId(caseId).getData());
    }

    private List<CaseChangeTargetCommittee> fetchTargetCommittee(List<String> targetIds) {
        if (targetIds.isEmpty()) {
            return List.of();
        }
        return safeList(caseChangeTargetCommitteeClient.findByCaseChangeTargetIds(targetIds).getData());
    }

    private List<CommitteeMember> buildCommitteeMembers(List<CaseChangeTargetCommittee> rows) {
        return rows.stream()
                .map(row -> CommitteeMember.builder()
                        .staffCode(row.getStaffCode())
                        .position(row.getProposedPosition())
                        .status(ECommitteeMemberStatus.OFFICIAL.getId())
                        .build())
                .toList();
    }

    private List<String> fetchSourceOrganizationIds(String caseId) {
        List<CaseOrganizationSummaryResponse> links = safeList(caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());
        return links.stream()
                .filter(link -> Objects.equals(link.getLinkRole(), ELinkRole.SOURCE.getId()))
                .map(CaseOrganizationSummaryResponse::getOrganizationId)
                .toList();
    }

    /** Chưa có quy tắc sinh mã chính thức cho PMDV_ORGANIZATION — sequential-per-prefix, cùng kiểu Case.generateCaseCode(). */
    private String generateOrganizationCode() {
        String prefix = ORGANIZATION_CODE_PREFIX + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Optional<Organization> latest = organizationClient.findLatestByPrefix(prefix + "-").getData();
        int nextSeq = 1;
        if (latest.isPresent()) {
            String latestCode = latest.get().getOrganizationCode();
            nextSeq = Integer.parseInt(latestCode.substring(latestCode.lastIndexOf('-') + 1)) + 1;
        }
        return prefix + "-" + String.format("%04d", nextSeq);
    }

    /** Re-validate ngay trước khi tạo — giữa lúc nhập ở Bước 1 và lúc Hoàn thành có thể có TCĐ khác giành mất tên. */
    private void validateOrganizationNameUnique(Map<String, String> errors, String fieldKey, String organizationName) {
        Boolean exists = organizationClient.existsActiveByName(organizationName, EOperationStatus.ACTIVE.getId()).getData();
        if (Boolean.TRUE.equals(exists)) {
            errors.put(fieldKey, "ERR-SC02-05: Tên tổ chức đảng đã trùng với 1 tổ chức đang Hoạt động");
        }
    }

    /** Tra hàng loạt (IN) qua OrganizationTypeClient.findAllById — tránh N+1 khi Chia tách có nhiều targets. */
    private void validateOrganizationTypesExist(Map<String, String> errors, List<CaseChangeTarget> targets) {
        List<String> organizationTypeIds = targets.stream()
                .map(CaseChangeTarget::getOrganizationTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<String> existingOrganizationTypeIds = organizationTypeIds.isEmpty() ? Set.of()
                : safeList(organizationTypeClient.findAllById(organizationTypeIds).getData()).stream()
                        .map(OrganizationType::getId)
                        .collect(Collectors.toSet());

        for (int i = 0; i < targets.size(); i++) {
            String organizationTypeId = targets.get(i).getOrganizationTypeId();
            if (organizationTypeId != null && !existingOrganizationTypeIds.contains(organizationTypeId)) {
                errors.put("targets[" + i + "].organizationTypeId", "ERR-SC06-02: organization_type_id không thuộc danh mục loại hình tổ chức đảng");
            }
        }
    }

    private void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private CompleteCaseResponse buildResponse(String caseId, String statusId, CaseCompleteResult result) {
        List<CompletedOrganizationResponse> organizations = safeList(result.getNewOrganizations()).stream()
                .map(o -> new CompletedOrganizationResponse(o.getId(), o.getOrganizationCode()))
                .toList();
        return new CompleteCaseResponse(caseId, statusId, organizations, safeList(result.getAffectedOrganizationIds()));
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
