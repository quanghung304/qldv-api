package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseChangeClient;
import com.agribank.qldv_api.gateway.CaseChangeTargetClient;
import com.agribank.qldv_api.gateway.CaseChangeTargetCommitteeClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.CommitteeMemberClient;
import com.agribank.qldv_api.gateway.EmployeeInfoClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.CaseChangeCommitteeMemberRequest;
import com.agribank.qldv_api.request.casemgmt.CaseChangeRequest;
import com.agribank.qldv_api.request.casemgmt.CaseChangeTargetRequest;
import com.agribank.qldv_api.response.casemgmt.CaseChangeDetailResponse;
import com.agribank.qldv_api.response.casemgmt.CaseChangeResponse;
import com.agribank.qldv_api.response.casemgmt.CaseChangeTargetDetailResponse;
import com.agribank.qldv_api.response.casemgmt.ProposedCommitteeMemberDetailResponse;
import com.agribank.qldv_api.workflow.WorkflowAssigneeGuard;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseChange;
import com.agribank.qldvutils.entity.CaseChangeTarget;
import com.agribank.qldvutils.entity.CaseChangeTargetCommittee;
import com.agribank.qldvutils.entity.CaseOrganization;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.enums.ECommitteeMemberStatus;
import com.agribank.qldvutils.enums.ECommitteePosition;
import com.agribank.qldvutils.enums.ELinkRole;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.casemgmt.CaseChangePersistRequest;
import com.agribank.qldvutils.request.casemgmt.CaseChangeTargetEntry;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import com.agribank.qldvutils.response.organization.CommitteeMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service dùng chung cho API-SC08-01 (tạo mới, Bước 1 SC-08) và API-SC08-02 (cập nhật một phần
 * khi hồ sơ còn ở bước 1) — 5 nghiệp vụ biến động TCĐ (Giải thể/Sáp nhập/Hợp nhất/Chia
 * tách/Đổi tên) dùng CHUNG 1 entity {@link CaseChange}/1 cặp API, field hiển thị/bắt buộc ĐỘNG
 * theo case_type (đọc {@code CaseType.code}), cùng thiết kế "1 hàm dùng chung nhận biến thể làm
 * tham số tường minh" như {@code EstablishmentCaseService} (coding-convention.md mục 11):
 * {@code authorityLevel}/{@code originFlow} do CONTROLLER truyền vào — service không tự suy luận.
 *
 * BR-SC08-01 (ràng buộc số lượng organizationIds theo case_type) lookup trực tiếp
 * {@code PMDV_CASE_TYPE.min/max_organization_count} — KHÔNG hardcode ngưỡng theo từng case_type
 * trong code (đúng coding-convention.md mục 11, khác với gợi ý liệt kê số cứng trong đặc tả gốc).
 *
 * {@code DISSOLVE/MERGE/CONSOLIDATE/SPLIT_CASE_TYPE_CODE} là 4 mã case_type CẦN phân biệt riêng —
 * theo cùng convention với {@code ESTABLISH_CASE_TYPE_CODE} của EstablishmentCaseService. Repo
 * hiện KHÔNG có enum liệt kê đủ 9 mã case_type nên các hằng số này là GIẢ ĐỊNH — cần đối chiếu lại
 * với dữ liệu PMDV_CASE_TYPE.code thật đã seed trước khi đưa vào production.
 *
 * {@code targets} (Sáp nhập/Hợp nhất=1 phần tử, Chia tách≥2, Giải thể/Đổi tên=0) — nhận NGAY Ở
 * BƯỚC 1 (không đợi tới API-SC06-02/Hoàn thành mới nhập) để `/complete` không cần request body,
 * đồng nhất với Thành lập/Giải thể/Đổi tên. Đây là quyết định thiết kế đổi từ bản đầu (targets
 * nhập ở /complete) sau khi review — {@code organizationTypeId}/{@code committee} của TCĐ đích vẫn
 * nhận qua field riêng (không sửa lại {@code proposedTargetName} cũ) vì Chia tách cần N tên khác
 * nhau, 1 field text đơn không đủ.
 *
 * Toàn bộ thao tác GHI (Case + CaseChange + CaseOrganization + CaseChangeTarget(Committee)) gói
 * thành 1 {@link CaseChangePersistRequest} gửi xuống qldv-db bằng ĐÚNG 1 lệnh
 * ({@code CaseChangeClient.persist}), chạy trong 1 transaction ({@code CaseChangePersistService}).
 */
@Service
@RequiredArgsConstructor
public class CaseChangeService {
    private static final String DISSOLVE_CASE_TYPE_CODE = "DISSOLVE";
    private static final String MERGE_CASE_TYPE_CODE = "MERGE";
    private static final String CONSOLIDATE_CASE_TYPE_CODE = "CONSOLIDATE";
    private static final String SPLIT_CASE_TYPE_CODE = "SPLIT";
    private static final int MAX_PROPOSED_TARGET_NAME_LENGTH = 250;

    private final CaseClient caseClient;
    private final CaseChangeClient caseChangeClient;
    private final CaseChangeTargetClient caseChangeTargetClient;
    private final CaseChangeTargetCommitteeClient caseChangeTargetCommitteeClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final CaseTypeClient caseTypeClient;
    private final OrganizationClient organizationClient;
    private final OrganizationTypeClient organizationTypeClient;
    private final CommitteeMemberClient committeeMemberClient;
    private final EmployeeInfoClient employeeInfoClient;
    private final UserService userService;
    private final EstablishmentCaseService establishmentCaseService;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;

    public CaseChangeResponse createCaseChange(CaseChangeRequest request, Integer authorityLevel, String originFlow) {
        UserDetailsImpl user = requireUser();
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        CaseType caseType = resolveCaseTypeForCreate(request.getCaseTypeId(), errors);
        if (caseType == null) {
            throw new FieldValidationException(errors);
        }
        boolean isDissolve = DISSOLVE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isMerge = MERGE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isConsolidate = CONSOLIDATE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isSplit = SPLIT_CASE_TYPE_CODE.equals(caseType.getCode());

        String proposedTargetName = isDissolve ? null : request.getProposedTargetName();
        String survivorOrganizationId = isMerge ? request.getSurvivorOrganizationId() : null;

        validateSurvivorApplicability(errors, request.getSurvivorOrganizationId(), isMerge);

        if (!isDissolve) {
            requireProposedTargetName(errors, proposedTargetName);
        }

        validateOrganizationCount(errors, caseType, request.getOrganizationIds());
        List<Organization> organizations = validateOrganizationsActive(errors, request.getOrganizationIds());

        if (isMerge) {
            validateSurvivorInList(errors, survivorOrganizationId, request.getOrganizationIds());
        }

        if (!isDissolve && proposedTargetName != null) {
            validateOrganizationNameUnique(errors, "proposedTargetName", proposedTargetName);
        }

        validateTargetsCount(errors, request.getTargets(), isMerge, isConsolidate, isSplit);
        if (errors.isEmpty() && (isMerge || isConsolidate || isSplit)) {
            validateTargetsAgainstDb(errors, request.getTargets());
        }
        if (errors.isEmpty() && isSplit) {
            validateSplitTargets(errors, request.getTargets(), organizations, request.getOrganizationIds());
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        Case newCase = Case.builder()
                .caseCode(generateCaseCode(caseType.getCode()))
                .caseTypeId(caseType.getId())
                .authorityLevel(authorityLevel)
                .originFlow(originFlow)
                .statusId(establishmentCaseService.initialStatusForFlow(originFlow))
                .createdBy(user.getId())
                // Cùng nguyên tắc với EstablishmentCaseService: bước 1 luôn do chính người tạo
                // tiếp tục thao tác (không có trong prompt gốc của task assigned-user, tự áp dụng
                // nhất quán cho cả hồ sơ biến động — xem báo cáo cuối task).
                .assignedUserId(user.getId())
                .proposedOrganizationName(proposedTargetName)
                .build();

        CaseChange caseChange = CaseChange.builder()
                .proposedTargetName(proposedTargetName)
                .survivorOrganizationId(survivorOrganizationId)
                .boardDecisionNo(request.getBoardDecisionNo())
                .boardDecisionDate(request.getBoardDecisionDate())
                .affectedMemberCount(sumMemberCount(organizations))
                .affectedCommitteeMemberCount(sumCommitteeMemberCount(organizations))
                .build();

        CaseChangePersistRequest persistRequest = new CaseChangePersistRequest();
        persistRequest.setCaseEntity(newCase);
        persistRequest.setCaseChange(caseChange);
        persistRequest.setOrganizations(buildCaseOrganizationRows(request.getOrganizationIds()));
        persistRequest.setReplaceOrganizations(true);
        persistRequest.setTargets(buildTargetEntries(request.getTargets()));
        persistRequest.setReplaceTargets(true);

        Case savedCase = caseChangeClient.persist(persistRequest).getData();

        return new CaseChangeResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    /**
     * API-SC08-02. Cập nhật MỘT PHẦN — field null trong request nghĩa là "không đổi". Chỉ cho
     * phép khi status_id đang đúng bước 1 CỦA ĐÚNG LUỒNG hồ sơ này (A-01/B-01/C-01 tuỳ
     * origin_flow đã lưu — KHÔNG hardcode A-01. caseTypeId
     * BẤT BIẾN, luôn lấy từ hồ sơ gốc — request.getCaseTypeId() (nếu có gửi) bị bỏ qua hoàn toàn.
     * {@code targets}: null = không đổi (giữ nguyên danh sách cũ); non-null (kể cả rỗng) = thay
     * TOÀN BỘ bằng danh sách mới — cùng ngữ nghĩa "tất cả hoặc không gì" với {@code organizationIds}
     * (không merge từng phần tử).
     * API-SC08-02. Cập nhật MỘT PHẦN — field null trong request nghĩa là "không đổi". Cho phép
     * khi status_id đang đúng bước 1 CỦA ĐÚNG LUỒNG hồ sơ này (A-01/B-01/C-01 tuỳ origin_flow đã
     * lưu — KHÔNG hardcode A-01, xem {@link EstablishmentCaseService#initialStatusForFlow}) HOẶC
     * đang ở bước "Trình kiểm soát" kế tiếp (A-02/B-02/C-02 —
     * {@link EstablishmentCaseService#controlStatusForFlow}) để Kiểm soát viên (R-KS/R-KSCS)
     * đang được giao xử lý hồ sơ ở bước kiểm soát cũng sửa được. caseTypeId BẤT BIẾN, luôn lấy từ
     * hồ sơ gốc — request.getCaseTypeId() (nếu có gửi) bị bỏ qua hoàn toàn.
     */
    public CaseChangeResponse updateCaseChange(String caseId, CaseChangeRequest request) {
        UserDetailsImpl user = requireUser();
        Case existingCase = caseClient.findById(caseId).getData().orElse(null);
        if (existingCase == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }

        String initialStatus = establishmentCaseService.initialStatusForFlow(existingCase.getOriginFlow());
        String controlStatus = establishmentCaseService.controlStatusForFlow(existingCase.getOriginFlow());
        if (!initialStatus.equals(existingCase.getStatusId()) && !controlStatus.equals(existingCase.getStatusId())) {
            throw new ForbiddenException("BR-SC08-06: Hồ sơ không ở bước 1 (" + initialStatus + ") hoặc bước kiểm soát ("
                    + controlStatus + "), không thể chỉnh sửa");
        }
        workflowAssigneeGuard.requireAssignee(existingCase, user.getId());

        CaseChange existingChange = caseChangeClient.findByCaseId(caseId).getData().orElse(
                CaseChange.builder().caseId(caseId).build());

        CaseType caseType = requireCaseType(existingCase.getCaseTypeId());
        boolean isDissolve = DISSOLVE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isMerge = MERGE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isConsolidate = CONSOLIDATE_CASE_TYPE_CODE.equals(caseType.getCode());
        boolean isSplit = SPLIT_CASE_TYPE_CODE.equals(caseType.getCode());

        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        boolean replaceOrganizations = request.getOrganizationIds() != null;
        List<String> effectiveOrganizationIds = replaceOrganizations
                ? request.getOrganizationIds() : currentOrganizationIds(caseId);

        String requestedSurvivorId = request.getSurvivorOrganizationId();
        String effectiveSurvivorId = isMerge
                ? (requestedSurvivorId != null ? requestedSurvivorId : existingChange.getSurvivorOrganizationId())
                : null;
        String effectiveProposedTargetName = isDissolve ? null
                : (request.getProposedTargetName() != null ? request.getProposedTargetName() : existingChange.getProposedTargetName());
        String effectiveBoardDecisionNo = request.getBoardDecisionNo() != null
                ? request.getBoardDecisionNo() : existingChange.getBoardDecisionNo();
        LocalDate effectiveBoardDecisionDate = request.getBoardDecisionDate() != null
                ? request.getBoardDecisionDate() : existingChange.getBoardDecisionDate();

        if (isMerge) {
            if (effectiveSurvivorId == null || effectiveSurvivorId.isBlank()) {
                errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId không được để trống với nghiệp vụ Sáp nhập");
            }
        } else if (requestedSurvivorId != null && !requestedSurvivorId.isBlank()) {
            errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId chỉ áp dụng cho nghiệp vụ Sáp nhập");
        }

        if (!isDissolve) {
            requireProposedTargetName(errors, effectiveProposedTargetName);
        }

        validateOrganizationCount(errors, caseType, effectiveOrganizationIds);
        List<Organization> organizations = validateOrganizationsActive(errors, effectiveOrganizationIds);

        if (isMerge) {
            validateSurvivorInList(errors, effectiveSurvivorId, effectiveOrganizationIds);
        }

        boolean nameChanged = effectiveProposedTargetName != null
                && (existingChange.getProposedTargetName() == null
                    || !effectiveProposedTargetName.equalsIgnoreCase(existingChange.getProposedTargetName()));
        if (!isDissolve && nameChanged) {
            validateOrganizationNameUnique(errors, "proposedTargetName", effectiveProposedTargetName);
        }

        boolean replaceTargets = request.getTargets() != null;
        if (replaceTargets) {
            validateTargetsCount(errors, request.getTargets(), isMerge, isConsolidate, isSplit);
            if (errors.isEmpty() && (isMerge || isConsolidate || isSplit)) {
                validateTargetsAgainstDb(errors, request.getTargets());
            }
            if (errors.isEmpty() && isSplit) {
                validateSplitTargets(errors, request.getTargets(), organizations, effectiveOrganizationIds);
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        if (effectiveProposedTargetName != null
                && !effectiveProposedTargetName.equals(existingCase.getProposedOrganizationName())) {
            existingCase.setProposedOrganizationName(effectiveProposedTargetName);
        } else if (isDissolve) {
            existingCase.setProposedOrganizationName(null);
        }

        existingChange.setProposedTargetName(effectiveProposedTargetName);
        existingChange.setSurvivorOrganizationId(effectiveSurvivorId);
        existingChange.setBoardDecisionNo(effectiveBoardDecisionNo);
        existingChange.setBoardDecisionDate(effectiveBoardDecisionDate);
        existingChange.setAffectedMemberCount(sumMemberCount(organizations));
        existingChange.setAffectedCommitteeMemberCount(sumCommitteeMemberCount(organizations));

        CaseChangePersistRequest persistRequest = new CaseChangePersistRequest();
        persistRequest.setCaseEntity(existingCase);
        persistRequest.setCaseChange(existingChange);
        persistRequest.setOrganizations(replaceOrganizations ? buildCaseOrganizationRows(effectiveOrganizationIds) : null);
        persistRequest.setReplaceOrganizations(replaceOrganizations);
        persistRequest.setTargets(replaceTargets ? buildTargetEntries(request.getTargets()) : null);
        persistRequest.setReplaceTargets(replaceTargets);

        Case savedCase = caseChangeClient.persist(persistRequest).getData();

        return new CaseChangeResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    /**
     * GET /cases/{id}/change — chi tiết đầy đủ Bước 1 SC-08, cùng pattern với
     * {@code EstablishmentCaseService#getEstablishmentCaseDetail}. Hồ sơ tồn tại nhưng chưa từng
     * lưu CaseChange (trường hợp hiếm) thì các field CaseChange trả về null thay vì lỗi 404 — 404
     * chỉ áp dụng khi chính PMDV_CASE không tồn tại.
     */
    public CaseChangeDetailResponse getCaseChangeDetail(String caseId) {
        CaseListItemResponse caseInfo = caseClient.findDetailById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));

        CaseChange caseChange = caseChangeClient.findByCaseId(caseId).getData().orElse(null);

        List<CaseOrganizationSummaryResponse> organizations = safeList(
                caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData()).stream()
                .filter(link -> Objects.equals(link.getLinkRole(), ELinkRole.SOURCE.getId()))
                .toList();

        List<CaseChangeTarget> targets = safeList(caseChangeTargetClient.findByCaseId(caseId).getData());

        return CaseChangeDetailResponse.builder()
                .caseId(caseInfo.getCaseId())
                .caseCode(caseInfo.getCaseCode())
                .caseTypeId(caseInfo.getCaseTypeId())
                .caseTypeName(caseInfo.getCaseTypeName())
                .statusId(caseInfo.getStatusId())
                .statusName(caseInfo.getStatusName())
                .authorityLevel(caseInfo.getAuthorityLevel())
                .originFlow(caseInfo.getOriginFlow())
                .createdBy(caseInfo.getCreatedBy())
                .createdByName(caseInfo.getCreatedByName())
                .createdAt(caseInfo.getCreatedAt())
                .updatedAt(caseInfo.getUpdatedAt())
                .completedAt(caseInfo.getCompletedAt())
                .proposedOrganizationName(caseInfo.getProposedOrganizationName())
                .organizations(organizations)
                .survivorOrganizationId(caseChange != null ? caseChange.getSurvivorOrganizationId() : null)
                .boardDecisionNo(caseChange != null ? caseChange.getBoardDecisionNo() : null)
                .boardDecisionDate(caseChange != null ? caseChange.getBoardDecisionDate() : null)
                .affectedMemberCount(caseChange != null ? caseChange.getAffectedMemberCount() : null)
                .affectedCommitteeMemberCount(caseChange != null ? caseChange.getAffectedCommitteeMemberCount() : null)
                .targets(resolveTargets(targets))
                .build();
    }

    /** organizationTypeId + cấp ủy dự kiến của TỪNG target tra hàng loạt (IN) — tránh N+1 khi Chia tách có nhiều targets. */
    private List<CaseChangeTargetDetailResponse> resolveTargets(List<CaseChangeTarget> targets) {
        if (targets.isEmpty()) {
            return List.of();
        }

        List<String> organizationTypeIds = targets.stream()
                .map(CaseChangeTarget::getOrganizationTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> typeNameById = organizationTypeIds.isEmpty() ? Map.of()
                : safeList(organizationTypeClient.findAllById(organizationTypeIds).getData()).stream()
                        .collect(Collectors.toMap(OrganizationType::getId, OrganizationType::getName, (a, b) -> a));

        List<String> targetIds = targets.stream().map(CaseChangeTarget::getId).toList();
        List<CaseChangeTargetCommittee> committeeRows = safeList(
                caseChangeTargetCommitteeClient.findByCaseChangeTargetIds(targetIds).getData());
        List<String> staffCodes = committeeRows.stream().map(CaseChangeTargetCommittee::getStaffCode).distinct().toList();
        Map<String, String> staffNameByCode = staffCodes.isEmpty() ? Map.of()
                : safeList(employeeInfoClient.findByEmpnos(staffCodes).getData()).stream()
                        .collect(Collectors.toMap(EmployeeInfoDto::getStaffCode, EmployeeInfoDto::getFullName, (a, b) -> a));
        Map<String, List<CaseChangeTargetCommittee>> committeeByTargetId = committeeRows.stream()
                .collect(Collectors.groupingBy(CaseChangeTargetCommittee::getCaseChangeTargetId));

        return targets.stream()
                .map(target -> {
                    List<ProposedCommitteeMemberDetailResponse> committee = safeList(committeeByTargetId.get(target.getId())).stream()
                            .map(row -> new ProposedCommitteeMemberDetailResponse(
                                    row.getStaffCode(),
                                    staffNameByCode.get(row.getStaffCode()),
                                    resolvePositionName(row.getProposedPosition())))
                            .toList();
                    return new CaseChangeTargetDetailResponse(target.getId(), target.getOrganizationName(),
                            target.getOrganizationTypeId(), typeNameById.get(target.getOrganizationTypeId()),
                            target.getMemberCount(), committee);
                })
                .toList();
    }

    private String resolvePositionName(Integer positionId) {
        if (positionId == null) {
            return null;
        }
        for (ECommitteePosition position : ECommitteePosition.values()) {
            if (position.getId() == positionId) {
                return position.name();
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- helpers (đều cần DB)

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private CaseType resolveCaseTypeForCreate(String caseTypeId, Map<String, String> errors) {
        if (caseTypeId == null || caseTypeId.isBlank()) {
            errors.put("caseTypeId", "ERR-SC08-01: caseTypeId không được để trống");
            return null;
        }
        Optional<CaseType> caseType = caseTypeClient.findById(caseTypeId).getData();
        if (caseType.isEmpty()) {
            errors.put("caseTypeId", "ERR-SC08-01: caseTypeId không thuộc danh mục loại nghiệp vụ hồ sơ");
            return null;
        }
        return caseType.get();
    }

    private CaseType requireCaseType(String caseTypeId) {
        return caseTypeClient.findById(caseTypeId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy danh mục loại nghiệp vụ (case_type_id=" + caseTypeId + ")"));
    }

    /** BR-SC08-01 — ngưỡng lookup từ PMDV_CASE_TYPE.min/max_organization_count, KHÔNG hardcode theo case_type. */
    private void validateOrganizationCount(Map<String, String> errors, CaseType caseType, List<String> organizationIds) {
        int count = organizationIds == null ? 0 : organizationIds.size();
        Integer min = caseType.getMinOrganizationCount();
        Integer max = caseType.getMaxOrganizationCount();
        boolean tooFew = min != null && count < min;
        boolean tooMany = max != null && count > max;
        if (tooFew || tooMany) {
            String rule = describeRange(min, max);
            errors.put("organizationIds", "ERR-SC08-02: Số lượng TCĐ đã chọn phải " + rule
                    + " theo loại hình nghiệp vụ '" + caseType.getName() + "' (BR-SC08-01)");
        }
    }

    private String describeRange(Integer min, Integer max) {
        if (min != null && max != null && min.equals(max)) {
            return "bằng " + min;
        }
        if (min != null && max != null) {
            return "từ " + min + " đến " + max;
        }
        if (min != null) {
            return ">= " + min;
        }
        return "<= " + max;
    }

    /**
     * Tồn tại + đang operation_status=ACTIVE (field 2 SC-08: "chỉ hiển thị TCĐ đang Hoạt động").
     * Tra hàng loạt (IN) qua OrganizationClient.findAllById — tránh N+1. Trả về danh sách theo
     * ĐÚNG thứ tự organizationIds để dùng tính snapshot; rỗng nếu có lỗi (caller không dùng tiếp).
     */
    private List<Organization> validateOrganizationsActive(Map<String, String> errors, List<String> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        List<Organization> found = safeList(organizationClient.findAllById(organizationIds).getData());
        Map<String, Organization> byId = found.stream()
                .collect(Collectors.toMap(Organization::getId, o -> o, (a, b) -> a));
        List<String> missing = organizationIds.stream().filter(id -> !byId.containsKey(id)).distinct().toList();

        if (!missing.isEmpty()) {
            errors.put("organizationIds", "ERR-SC08-04: organizationIds không tồn tại: " + String.join(", ", missing));
            return List.of();
        }

        List<String> notActive = organizationIds.stream()
                .filter(id -> !Objects.equals(byId.get(id).getOperationStatus(), EOperationStatus.ACTIVE.getId()))
                .distinct().toList();

        if (!notActive.isEmpty()) {
            errors.put("organizationIds", "ERR-SC08-04: TCĐ không ở trạng thái Hoạt động: " + String.join(", ", notActive));
            return List.of();
        }

        return organizationIds.stream().map(byId::get).toList();
    }

    private void validateOrganizationNameUnique(Map<String, String> errors, String fieldKey, String organizationName) {
        Boolean exists = organizationClient
                .existsActiveByName(organizationName, EOperationStatus.ACTIVE.getId())
                .getData();

        if (Boolean.TRUE.equals(exists)) {
            errors.put(fieldKey, "ERR-SC02-05: " + fieldKey + " đã trùng tên 1 tổ chức đảng đang Hoạt động");
        }
    }

    /** BR-SC08-03 — bắt buộc với mọi case_type trừ Giải thể; giới hạn độ dài áp dụng chung. */
    private void requireProposedTargetName(Map<String, String> errors, String proposedTargetName) {
        if (proposedTargetName == null || proposedTargetName.isBlank()) {
            errors.put("proposedTargetName", "ERR-SC08-03: proposedTargetName không được để trống với loại hình nghiệp vụ này");
        } else if (proposedTargetName.length() > Constants.MAX_ORGANIZATION_NAME_LENGTH) {
            errors.put("proposedTargetName", "ERR-SC08-03: proposedTargetName không được vượt quá "
                    + Constants.MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
        }
    }

    /** survivorOrganizationId chỉ áp dụng cho Sáp nhập — client gửi field này ở case_type khác là dấu hiệu gọi sai, KHÔNG lặng lẽ bỏ qua. */
    private void validateSurvivorApplicability(Map<String, String> errors, String rawSurvivorOrganizationId, boolean isMerge) {
        if (isMerge) {
            if (rawSurvivorOrganizationId == null || rawSurvivorOrganizationId.isBlank()) {
                errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId không được để trống với nghiệp vụ Sáp nhập");
            }
        } else if (rawSurvivorOrganizationId != null && !rawSurvivorOrganizationId.isBlank()) {
            errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId chỉ áp dụng cho nghiệp vụ Sáp nhập");
        }
    }

    private void validateSurvivorInList(Map<String, String> errors, String survivorOrganizationId, List<String> organizationIds) {
        if (survivorOrganizationId != null && (organizationIds == null || !organizationIds.contains(survivorOrganizationId))) {
            errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId phải nằm trong danh sách organizationIds");
        }
    }

    /** Sáp nhập/Hợp nhất=đúng 1, Chia tách=>=2, Giải thể/Đổi tên=0 (chưa biết case_type khác thì chặn luôn nếu có gửi). */
    private void validateTargetsCount(Map<String, String> errors, List<CaseChangeTargetRequest> targets,
                                       boolean isMerge, boolean isConsolidate, boolean isSplit) {
        int count = targets == null ? 0 : targets.size();
        if (isMerge || isConsolidate) {
            if (count != 1) {
                errors.put("targets", "ERR-SC08-08: Sáp nhập/Hợp nhất cần đúng 1 phần tử trong targets");
            }
        } else if (isSplit) {
            if (count < 2) {
                errors.put("targets", "ERR-SC08-08: Chia tách cần >= 2 phần tử trong targets");
            }
        } else if (count > 0) {
            errors.put("targets", "ERR-SC08-08: targets chỉ áp dụng cho Sáp nhập/Hợp nhất/Chia tách");
        }
    }

    /**
     * organizationTypeId tra hàng loạt (IN) qua OrganizationTypeClient.findAllById — tránh N+1
     * khi Chia tách có nhiều targets. organizationName KHÔNG batch được (existsActiveByName chỉ
     * nhận 1 tên/lần, chưa có endpoint batch) — targets thực tế luôn nhỏ (Sáp nhập/Hợp nhất=1,
     * Chia tách hiếm khi quá vài phần tử) nên N lệnh gọi riêng cho tên là chấp nhận được, không
     * đáng thêm 1 tầng API mới chỉ để tiết kiệm vài lệnh gọi.
     */
    private void validateTargetsAgainstDb(Map<String, String> errors, List<CaseChangeTargetRequest> targets) {
        List<String> organizationTypeIds = targets.stream()
                .map(CaseChangeTargetRequest::getOrganizationTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Set<String> existingOrganizationTypeIds = organizationTypeIds.isEmpty() ? Set.of()
                : safeList(organizationTypeClient.findAllById(organizationTypeIds).getData()).stream()
                        .map(OrganizationType::getId)
                        .collect(Collectors.toSet());

        for (int i = 0; i < targets.size(); i++) {
            CaseChangeTargetRequest target = targets.get(i);
            String prefix = "targets[" + i + "].";
            if (target.getOrganizationTypeId() != null && !existingOrganizationTypeIds.contains(target.getOrganizationTypeId())) {
                errors.put(prefix + "organizationTypeId", "ERR-SC08-04: organizationTypeId không thuộc danh mục loại hình tổ chức đảng");
            }
            if (target.getOrganizationName() != null) {
                validateOrganizationNameUnique(errors, prefix + "organizationName", target.getOrganizationName());
            }
        }
    }

    /** Chia tách — SUM(targets[].memberCount) phải khớp member_count TCĐ nguồn + phân bổ đủ cấp ủy OFFICIAL của nguồn, không thiếu/dư/trùng. */
    private void validateSplitTargets(Map<String, String> errors, List<CaseChangeTargetRequest> targets,
                                       List<Organization> sourceOrganizations, List<String> sourceOrganizationIds) {
        int sum = targets.stream().mapToInt(t -> t.getMemberCount() == null ? 0 : t.getMemberCount()).sum();
        int expected = sumMemberCount(sourceOrganizations);
        if (sum != expected) {
            errors.put("targets", "ERR-SC08-09: Tổng memberCount các TCĐ đích (" + sum + ") phải bằng member_count TCĐ nguồn (" + expected + ")");
        }

        if (sourceOrganizationIds == null || sourceOrganizationIds.size() != 1) {
            return;
        }
        List<CommitteeMemberResponse> sourceCommittee = safeList(
                committeeMemberClient.findByOrganizationIdAndStatus(sourceOrganizationIds.get(0), ECommitteeMemberStatus.OFFICIAL.getId()).getData());
        Set<String> expectedStaffCodes = sourceCommittee.stream().map(CommitteeMemberResponse::getStaffCode).collect(Collectors.toSet());

        List<String> providedStaffCodes = targets.stream()
                .flatMap(t -> safeList(t.getCommittee()).stream())
                .map(CaseChangeCommitteeMemberRequest::getStaffCode)
                .toList();

        Set<String> seen = new HashSet<>();
        List<String> duplicated = providedStaffCodes.stream().filter(code -> !seen.add(code)).distinct().toList();
        Set<String> providedSet = new HashSet<>(providedStaffCodes);
        List<String> missing = expectedStaffCodes.stream().filter(code -> !providedSet.contains(code)).toList();
        List<String> extra = providedSet.stream().filter(code -> !expectedStaffCodes.contains(code)).toList();

        if (!missing.isEmpty()) {
            errors.put("targets.committee.missing", "ERR-SC08-10: Thiếu cấp ủy chưa được phân bổ vào TCĐ đích nào: " + String.join(", ", missing));
        }
        if (!extra.isEmpty()) {
            errors.put("targets.committee.extra", "ERR-SC08-10: Có staff_code không thuộc cấp ủy TCĐ nguồn: " + String.join(", ", extra));
        }
        if (!duplicated.isEmpty()) {
            errors.put("targets.committee.duplicated", "ERR-SC08-10: Cấp ủy bị phân bổ trùng vào nhiều TCĐ đích: " + String.join(", ", duplicated));
        }
    }

    private List<CaseChangeTargetEntry> buildTargetEntries(List<CaseChangeTargetRequest> targets) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        return targets.stream().map(target -> {
            CaseChangeTargetEntry entry = new CaseChangeTargetEntry();
            entry.setTarget(CaseChangeTarget.builder()
                    .organizationName(target.getOrganizationName())
                    .organizationTypeId(target.getOrganizationTypeId())
                    .memberCount(target.getMemberCount())
                    .build());
            entry.setCommittee(buildTargetCommittee(target.getCommittee()));
            return entry;
        }).toList();
    }

    private List<CaseChangeTargetCommittee> buildTargetCommittee(List<CaseChangeCommitteeMemberRequest> committee) {
        if (committee == null || committee.isEmpty()) {
            return List.of();
        }
        return committee.stream()
                .map(member -> CaseChangeTargetCommittee.builder()
                        .staffCode(member.getStaffCode())
                        .proposedPosition(ECommitteePosition.valueOf(member.getPosition()).getId())
                        .build())
                .toList();
    }

    private List<String> currentOrganizationIds(String caseId) {
        List<CaseOrganizationSummaryResponse> current = safeList(caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());
        return current.stream()
                .filter(row -> Objects.equals(row.getLinkRole(), ELinkRole.SOURCE.getId()))
                .map(CaseOrganizationSummaryResponse::getOrganizationId)
                .toList();
    }

    private Integer sumMemberCount(List<Organization> organizations) {
        return organizations.stream().mapToInt(o -> nz(o.getMemberCount())).sum();
    }

    private Integer sumCommitteeMemberCount(List<Organization> organizations) {
        return organizations.stream().mapToInt(o -> nz(o.getCommitteeMemberCount())).sum();
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }

    /** Case chỉ có 1 dòng CaseOrganization/mỗi organizationId, link_role=SOURCE — kể cả survivorOrganizationId (xem prompt_S4-01 mục 2, bước 3). */
    private List<CaseOrganization> buildCaseOrganizationRows(List<String> organizationIds) {
        return organizationIds.stream()
                .map(id -> CaseOrganization.builder().organizationId(id).linkRole(ELinkRole.SOURCE.getId()).build())
                .toList();
    }

    /** Cùng cơ chế sinh mã case_code với EstablishmentCaseService.generateCaseCode — chưa tách hàm dùng chung, ngoài phạm vi task này. */
    private String generateCaseCode(String caseTypeCode) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String prefix = caseTypeCode + "-" + LocalDate.now().format(formatter);
        Optional<Case> latest = caseClient.findLatestByPrefix(prefix + "-").getData();
        int nextSeq = 1;
        if (latest.isPresent()) {
            String latestCode = latest.get().getCaseCode();
            nextSeq = Integer.parseInt(latestCode.substring(latestCode.lastIndexOf('-') + 1)) + 1;
        }
        return prefix + "-" + String.format("%04d", nextSeq);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
