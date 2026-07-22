package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.AttachmentClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.gateway.StaffClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.EstablishmentCaseRequest;
import com.agribank.qldv_api.request.casemgmt.LeadershipInfoRequest;
import com.agribank.qldv_api.request.casemgmt.ProposedCommitteeMemberRequest;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseResponse;
import com.agribank.qldvutils.entity.Attachment;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.entity.Staff;
import com.agribank.qldvutils.enums.ECommitteePosition;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.casemgmt.EstablishmentCasePersistRequest;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Service dùng chung cho API-SC02-01 (tạo mới, Bước 1 Thành lập TCĐ cấp Agribank) và API-SC02-02
 * (cập nhật một phần khi hồ sơ còn ở A-01) — thiết kế theo đúng RR-03 (prompt_S2-04_API_SC02.md
 * mục 2): 1 hàm dùng chung nhận {@code authorityLevel}/{@code allowedOrganizationTypeId} làm
 * input, KHÔNG rẽ nhánh business rule theo authorityLevel, KHÔNG hardcode ngưỡng member_count —
 * để Sprint 5 (SC-07, cấp cơ sở) chỉ cần thêm 1 lớp controller mỏng ép sẵn
 * {@code allowedOrganizationTypeId} + gọi lại nguyên vẹn service này.
 *
 * Toàn bộ thao tác GHI (Case + CaseEstablishment + committee + liên kết attachment) gói thành 1
 * {@link EstablishmentCasePersistRequest} và gửi xuống qldv-db bằng ĐÚNG 1 lệnh
 * ({@code CaseEstablishmentClient.persist}) — chạy trong 1 transaction ở qldv-db
 * ({@code CaseEstablishmentPersistService}), lỗi ở bất kỳ bước nào cũng rollback toàn bộ, không
 * còn 4 lệnh Feign rời rạc tự commit riêng như trước. Ở đây (qldv-api) chỉ làm nhiệm vụ đọc dữ
 * liệu để validate + build request — không tự thực hiện ghi DB nào cả.
 *
 * BR-SC02-06 (tự động kích hoạt Data Mapping/API-DOC-01 khi lưu đủ field bắt buộc) CHƯA được nối
 * ở đây — cơ chế sinh văn bản tự động (S2-03, API-DOC-01..04) chưa triển khai trong repo tại thời
 * điểm code task này; khi S2-03 sẵn sàng, gọi service đó ngay sau khi tạo/cập nhật thành công tại
 * đúng vị trí đánh dấu TODO bên dưới.
 */
@Service
@RequiredArgsConstructor
public class EstablishmentCaseService {
    private static final String ESTABLISH_CASE_TYPE_CODE = "ESTABLISH";
    private static final String INITIAL_STATUS_CODE = "A-01";

    private static final int MAX_STAFF_COUNT = 100000;
    private static final int MAX_LEADERSHIP_FREE_TEXT_LENGTH = 500;
    private static final int MAX_BOARD_DECISION_NO_LENGTH = 100;
    private static final int MAX_BOARD_DECISION_SUMMARY_LENGTH = 1000;
    private static final int MAX_ORGANIZATION_NAME_LENGTH = 250;
    private static final int MAX_COMMITTEE_STRUCTURE_LENGTH = 500;
    private static final int MAX_CONCLUSION_NO_LENGTH = 50;
    private static final long POLITICAL_STANDARD_VALIDITY_MONTHS = 6;
    private static final Set<String> VALID_POSITIONS = Set.of("SECRETARY", "DEPUTY_SECRETARY", "MEMBER");
    private static final Set<String> PERSONNEL_ATTACHMENT_TYPES = Set.of("PERSONNEL_PLAN", "MEMBER_LIST");

    private final CaseClient caseClient;
    private final CaseEstablishmentClient caseEstablishmentClient;
    private final CaseTypeClient caseTypeClient;
    private final OrganizationTypeClient organizationTypeClient;
    private final OrganizationClient organizationClient;
    private final StaffClient staffClient;
    private final AttachmentClient attachmentClient;
    private final UserService userService;

    /**
     * API-SC02-01. {@code allowedOrganizationTypeId} null ở Sprint 2 (client tự chọn trong danh
     * mục) — Sprint 5/SC-07 truyền cố định mã "Chi bộ trực thuộc đảng bộ cơ sở" (BR-SC07-01).
     * {@code originFlow} do CALLER (controller) truyền — service không tự suy ra từ
     * authorityLevel (mục 2.4 prompt): Sprint 2 luôn gọi với Constants.CASE_FLOW_BTCDU ("A").
     */
    public EstablishmentCaseResponse createEstablishmentCase(EstablishmentCaseRequest request, Integer authorityLevel,
                                                              String originFlow, String allowedOrganizationTypeId) {
        UserDetailsImpl user = requireUser();
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        String organizationTypeId = allowedOrganizationTypeId != null
                ? allowedOrganizationTypeId : request.getOrganizationTypeId();
        OrganizationType organizationType = validateOrganizationType(organizationTypeId, errors);

        requirePositiveInt(errors, "brcd", request.getBrcd(), null,
                "ERR-SC02-01: brcd (mã chi nhánh) không được để trống");
        requirePositiveInt(errors, "staffCount", request.getStaffCount(), MAX_STAFF_COUNT,
                "ERR-SC02-02: staff_count phải là số nguyên dương, tối đa " + MAX_STAFF_COUNT);
        validateLeadershipInfo(errors, request.getLeadershipInfo());
        requireText(errors, "boardDecisionNo", request.getBoardDecisionNo(), MAX_BOARD_DECISION_NO_LENGTH,
                "ERR-SC02-01: board_decision_no không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "boardDecisionDate", request.getBoardDecisionDate(),
                "ERR-SC02-04: board_decision_date không được để trống và không được sau ngày hiện tại");
        requireText(errors, "boardDecisionSummary", request.getBoardDecisionSummary(), MAX_BOARD_DECISION_SUMMARY_LENGTH,
                "ERR-SC02-01: board_decision_summary không được để trống, không vượt quá " + MAX_BOARD_DECISION_SUMMARY_LENGTH + " ký tự");
        requireText(errors, "proposedOrganizationName", request.getProposedOrganizationName(), MAX_ORGANIZATION_NAME_LENGTH,
                "ERR-SC02-01: proposed_organization_name không được để trống, không vượt quá " + MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
        validateOrganizationNameUnique(errors, request.getProposedOrganizationName());
        requirePositiveInt(errors, "memberCount", request.getMemberCount(), null, null);
        validateMemberCountThreshold(errors, request.getMemberCount(), organizationType);
        validateCommitteeMemberCount(errors, request.getCommitteeMemberCount(), request.getMemberCount());
        requireText(errors, "committeeStructure", request.getCommitteeStructure(), MAX_COMMITTEE_STRUCTURE_LENGTH,
                "ERR-SC02-01: committee_structure không được để trống, không vượt quá " + MAX_COMMITTEE_STRUCTURE_LENGTH + " ký tự");
        Map<String, Staff> staffByCode = validateProposedCommitteeMembers(errors,
                request.getProposedCommitteeMembers(), request.getCommitteeMemberCount());
        requireText(errors, "politicalStandardConclusionNo", request.getPoliticalStandardConclusionNo(), MAX_CONCLUSION_NO_LENGTH,
                "ERR-SC02-01: political_standard_conclusion_no không được để trống, không vượt quá " + MAX_CONCLUSION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "politicalStandardConclusionDate", request.getPoliticalStandardConclusionDate(),
                "ERR-SC02-04: political_standard_conclusion_date không được để trống và không được sau ngày hiện tại");
        validatePoliticalStandardValidity(warnings, request.getPoliticalStandardConclusionDate());
        validateAttachments(errors, warnings, request.getAttachmentIds());

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        CaseType caseType = requireEstablishCaseType();

        Case newCase = Case.builder()
                .caseCode(generateCaseCode(caseType.getCode()))
                .caseTypeId(caseType.getId())
                .authorityLevel(authorityLevel)
                .originFlow(originFlow)
                .statusId(INITIAL_STATUS_CODE)
                .createdBy(user.getId())
                .brcd(user.getBrcd())
                .proposedOrganizationName(request.getProposedOrganizationName())
                .build();

        CaseEstablishment establishment = CaseEstablishment.builder().build();
        applyUpdates(establishment, request, organizationTypeId);

        EstablishmentCasePersistRequest persistRequest = new EstablishmentCasePersistRequest();
        persistRequest.setCaseEntity(newCase);
        persistRequest.setEstablishment(establishment);
        persistRequest.setCommitteeMembers(buildCommitteeRows(request.getProposedCommitteeMembers(), staffByCode));
        persistRequest.setAttachmentIdsToLink(request.getAttachmentIds());

        Case savedCase = caseEstablishmentClient.persist(persistRequest).getData();

        // TODO(S2-03/BR-SC02-06): khi có đủ field bắt buộc, tự động gọi API-DOC-01 nội bộ để
        // kết xuất Tờ trình BTV + Phiếu xin ý kiến BTV. Chưa nối vì cơ chế Data Mapping (S2-03)
        // chưa triển khai trong repo — xem javadoc lớp này.

        return new EstablishmentCaseResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    /**
     * API-SC02-02. Cập nhật MỘT PHẦN — field null trong request nghĩa là "không đổi". BR-SC02-05:
     * chỉ cho phép khi status_id = A-01, chặn ở BE bất kể FE có ẩn field hay không.
     */
    public EstablishmentCaseResponse updateEstablishmentCase(String caseId, EstablishmentCaseRequest request,
                                                              String allowedOrganizationTypeId) {
        requireUser();
        Case existingCase = caseClient.findById(caseId).getData().orElse(null);
        if (existingCase == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }
        if (!INITIAL_STATUS_CODE.equals(existingCase.getStatusId())) {
            throw new ForbiddenException("BR-SC02-05: Hồ sơ không còn ở trạng thái Đang thực hiện (A-01), không thể chỉnh sửa");
        }

        CaseEstablishment existing = caseEstablishmentClient.findById(caseId).getData().orElse(
                CaseEstablishment.builder().caseId(caseId).build());

        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        String requestedOrganizationTypeId = allowedOrganizationTypeId != null
                ? allowedOrganizationTypeId : request.getOrganizationTypeId();
        String effectiveOrganizationTypeId = requestedOrganizationTypeId != null
                ? requestedOrganizationTypeId : existing.getOrganizationTypeId();
        OrganizationType organizationType = effectiveOrganizationTypeId != null
                ? validateOrganizationType(effectiveOrganizationTypeId, errors) : null;

        if (request.getBrcd() != null) {
            requirePositiveInt(errors, "brcd", request.getBrcd(), null,
                    "ERR-SC02-01: brcd (mã chi nhánh) không được để trống");
        }
        if (request.getStaffCount() != null) {
            requirePositiveInt(errors, "staffCount", request.getStaffCount(), MAX_STAFF_COUNT,
                    "ERR-SC02-02: staff_count phải là số nguyên dương, tối đa " + MAX_STAFF_COUNT);
        }
        if (request.getLeadershipInfo() != null) {
            validateLeadershipInfo(errors, request.getLeadershipInfo());
        }
        if (request.getBoardDecisionNo() != null) {
            requireText(errors, "boardDecisionNo", request.getBoardDecisionNo(), MAX_BOARD_DECISION_NO_LENGTH,
                    "ERR-SC02-01: board_decision_no không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        }
        if (request.getBoardDecisionDate() != null) {
            requirePastOrPresentDate(errors, "boardDecisionDate", request.getBoardDecisionDate(),
                    "ERR-SC02-04: board_decision_date không được sau ngày hiện tại");
        }
        if (request.getBoardDecisionSummary() != null) {
            requireText(errors, "boardDecisionSummary", request.getBoardDecisionSummary(), MAX_BOARD_DECISION_SUMMARY_LENGTH,
                    "ERR-SC02-01: board_decision_summary không được để trống, không vượt quá " + MAX_BOARD_DECISION_SUMMARY_LENGTH + " ký tự");
        }
        if (request.getProposedOrganizationName() != null) {
            requireText(errors, "proposedOrganizationName", request.getProposedOrganizationName(), MAX_ORGANIZATION_NAME_LENGTH,
                    "ERR-SC02-01: proposed_organization_name không được để trống, không vượt quá " + MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
            if (!request.getProposedOrganizationName().equalsIgnoreCase(existingCase.getProposedOrganizationName())) {
                validateOrganizationNameUnique(errors, request.getProposedOrganizationName());
            }
        }

        Integer effectiveMemberCount = request.getMemberCount() != null ? request.getMemberCount() : existing.getMemberCount();
        if (request.getMemberCount() != null) {
            requirePositiveInt(errors, "memberCount", request.getMemberCount(), null, null);
        }
        if (organizationType != null && effectiveMemberCount != null) {
            validateMemberCountThreshold(errors, effectiveMemberCount, organizationType);
        }

        Integer effectiveCommitteeMemberCount = request.getCommitteeMemberCount() != null
                ? request.getCommitteeMemberCount() : existing.getCommitteeMemberCount();
        if (request.getCommitteeMemberCount() != null || request.getMemberCount() != null) {
            validateCommitteeMemberCount(errors, effectiveCommitteeMemberCount, effectiveMemberCount);
        }
        if (request.getCommitteeStructure() != null) {
            requireText(errors, "committeeStructure", request.getCommitteeStructure(), MAX_COMMITTEE_STRUCTURE_LENGTH,
                    "ERR-SC02-01: committee_structure không được để trống, không vượt quá " + MAX_COMMITTEE_STRUCTURE_LENGTH + " ký tự");
        }

        Map<String, Staff> staffByCode = Map.of();
        boolean replaceCommitteeMembers = request.getProposedCommitteeMembers() != null;
        if (replaceCommitteeMembers) {
            staffByCode = validateProposedCommitteeMembers(errors, request.getProposedCommitteeMembers(), effectiveCommitteeMemberCount);
        }

        if (request.getPoliticalStandardConclusionNo() != null) {
            requireText(errors, "politicalStandardConclusionNo", request.getPoliticalStandardConclusionNo(), MAX_CONCLUSION_NO_LENGTH,
                    "ERR-SC02-01: political_standard_conclusion_no không được để trống, không vượt quá " + MAX_CONCLUSION_NO_LENGTH + " ký tự");
        }
        LocalDate effectiveConclusionDate = request.getPoliticalStandardConclusionDate() != null
                ? request.getPoliticalStandardConclusionDate() : existing.getPoliticalStandardConclusionDate();
        if (request.getPoliticalStandardConclusionDate() != null) {
            requirePastOrPresentDate(errors, "politicalStandardConclusionDate", request.getPoliticalStandardConclusionDate(),
                    "ERR-SC02-04: political_standard_conclusion_date không được sau ngày hiện tại");
        }
        validatePoliticalStandardValidity(warnings, effectiveConclusionDate);

        if (request.getAttachmentIds() != null) {
            validateAttachments(errors, warnings, request.getAttachmentIds());
        }
        // Nếu request không gửi attachmentIds: không đụng tới tệp đính kèm hiện có — chưa có
        // API tra cứu tệp theo case_id (thuộc phạm vi S2-05, chưa triển khai) nên không thể kiểm
        // tra lại BR-SC02-03 cho tệp CŨ ở lần cập nhật này; chỉ cảnh báo khi client gửi kèm
        // attachmentIds mới.

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        if (request.getProposedOrganizationName() != null
                && !request.getProposedOrganizationName().equals(existingCase.getProposedOrganizationName())) {
            existingCase.setProposedOrganizationName(request.getProposedOrganizationName());
        }

        applyUpdates(existing, request, requestedOrganizationTypeId);

        EstablishmentCasePersistRequest persistRequest = new EstablishmentCasePersistRequest();
        persistRequest.setCaseEntity(existingCase);
        persistRequest.setEstablishment(existing);
        persistRequest.setCommitteeMembers(replaceCommitteeMembers
                ? buildCommitteeRows(request.getProposedCommitteeMembers(), staffByCode) : null);
        persistRequest.setAttachmentIdsToLink(request.getAttachmentIds());

        Case savedCase = caseEstablishmentClient.persist(persistRequest).getData();

        // TODO(S2-03/BR-SC02-06): tương tự createEstablishmentCase — gọi API-DOC-01 khi đủ field.

        return new EstablishmentCaseResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    // ---------------------------------------------------------------- helpers

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private CaseType requireEstablishCaseType() {
        return caseTypeClient.findByCode(ESTABLISH_CASE_TYPE_CODE).getData()
                .orElseThrow(() -> new CommonException(
                        "Danh mục loại nghiệp vụ 'Thành lập' (case_type_id=ESTABLISH) chưa được seed — phụ thuộc S1-02"));
    }

    private OrganizationType validateOrganizationType(String organizationTypeId, Map<String, String> errors) {
        if (organizationTypeId == null || organizationTypeId.isBlank()) {
            errors.put("organizationTypeId", "ERR-SC02-06: organization_type_id không được để trống");
            return null;
        }
        Optional<OrganizationType> organizationType = organizationTypeClient.findById(organizationTypeId).getData();
        if (organizationType.isEmpty()) {
            errors.put("organizationTypeId", "ERR-SC02-06: organization_type_id không thuộc danh mục loại hình tổ chức đảng");
            return null;
        }
        return organizationType.get();
    }

    private void requireText(Map<String, String> errors, String field, String value, int maxLength, String message) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            errors.put(field, message);
        }
    }

    private void requirePositiveInt(Map<String, String> errors, String field, Integer value, Integer max, String message) {
        if (value == null || value <= 0 || (max != null && value > max)) {
            errors.put(field, message != null ? message : "ERR-SC02-02: " + field + " phải là số nguyên dương");
        }
    }

    private void requirePastOrPresentDate(Map<String, String> errors, String field, LocalDate value, String message) {
        if (value == null || value.isAfter(LocalDate.now())) {
            errors.put(field, message);
        }
    }

    private void validateLeadershipInfo(Map<String, String> errors, LeadershipInfoRequest leadershipInfo) {
        if (leadershipInfo == null) {
            return;
        }
        if (leadershipInfo.getStaffId() != null && !leadershipInfo.getStaffId().isBlank()) {
            Staff staff = staffClient.findByStaffCode(leadershipInfo.getStaffId()).getData();
            if (staff == null) {
                errors.put("leadershipInfo", "ERR-SC02-03: staff_id trong leadership_info không tồn tại ở hệ thống GA");
            }
        } else if (leadershipInfo.getFreeText() != null
                && leadershipInfo.getFreeText().length() > MAX_LEADERSHIP_FREE_TEXT_LENGTH) {
            errors.put("leadershipInfo", "ERR-SC02-03: leadership_info nhập tay không được vượt quá "
                    + MAX_LEADERSHIP_FREE_TEXT_LENGTH + " ký tự");
        }
    }

    private void validateOrganizationNameUnique(Map<String, String> errors, String proposedOrganizationName) {
        if (proposedOrganizationName == null || proposedOrganizationName.isBlank()) {
            return;
        }
        Boolean exists = organizationClient
                .existsActiveByName(proposedOrganizationName, EOperationStatus.ACTIVE.getId())
                .getData();
        if (Boolean.TRUE.equals(exists)) {
            errors.put("proposedOrganizationName", "ERR-SC02-05: proposed_organization_name đã trùng tên 1 tổ chức đảng đang Hoạt động");
        }
    }

    /** BR-SC02-01 — ngưỡng lookup từ PMDV_ORGANIZATION_TYPE.min_member_count, KHÔNG hardcode 3/30. */
    private void validateMemberCountThreshold(Map<String, String> errors, Integer memberCount, OrganizationType organizationType) {
        if (memberCount == null || organizationType == null || organizationType.getMinMemberCount() == null) {
            return;
        }
        if (memberCount < organizationType.getMinMemberCount()) {
            errors.put("memberCount", "ERR-SC02-07: member_count phải >= " + organizationType.getMinMemberCount()
                    + " theo loại hình tổ chức đảng đã chọn (BR-SC02-01)");
        }
    }

    private void validateCommitteeMemberCount(Map<String, String> errors, Integer committeeMemberCount, Integer memberCount) {
        if (committeeMemberCount == null || committeeMemberCount < 0) {
            errors.put("committeeMemberCount", "ERR-SC02-08: committee_member_count phải >= 0");
            return;
        }
        if (memberCount != null && committeeMemberCount > memberCount) {
            errors.put("committeeMemberCount", "ERR-SC02-08: committee_member_count không được vượt quá member_count");
        }
    }

    private Map<String, Staff> validateProposedCommitteeMembers(Map<String, String> errors,
                                                                 List<ProposedCommitteeMemberRequest> members,
                                                                 Integer committeeMemberCount) {
        if (members == null || members.isEmpty()) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: proposed_committee_members không được để trống");
            return Map.of();
        }
        if (committeeMemberCount != null && members.size() != committeeMemberCount) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: Số dòng proposed_committee_members phải bằng committee_member_count");
        }
        long secretaryCount = members.stream().filter(m -> "SECRETARY".equals(m.getProposedPosition())).count();
        if (secretaryCount > 1) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: Danh sách cấp ủy dự kiến chỉ được tối đa 1 SECRETARY");
        }
        boolean invalidPosition = members.stream()
                .anyMatch(m -> m.getProposedPosition() == null || !VALID_POSITIONS.contains(m.getProposedPosition()));
        if (invalidPosition) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: proposed_position phải thuộc SECRETARY/DEPUTY_SECRETARY/MEMBER");
        }

        List<String> staffIds = members.stream()
                .map(ProposedCommitteeMemberRequest::getStaffId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Staff> foundStaff = staffIds.isEmpty() ? List.of() : safeList(staffClient.findByStaffCodes(staffIds).getData());
        Map<String, Staff> staffByCode = foundStaff.stream()
                .collect(Collectors.toMap(Staff::getStaffCode, s -> s, (a, b) -> a));
        List<String> missing = staffIds.stream().filter(id -> !staffByCode.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-10: staff_id không hợp lệ/không tồn tại: " + String.join(", ", missing));
        }
        return staffByCode;
    }

    /** BR-SC02-02 — cảnh báo (không chặn lưu), quá 6 tháng kể từ ngày ban hành kết luận TCCT. */
    private void validatePoliticalStandardValidity(Map<String, String> warnings, LocalDate conclusionDate) {
        if (conclusionDate == null) {
            return;
        }
        if (conclusionDate.plusMonths(POLITICAL_STANDARD_VALIDITY_MONTHS).isBefore(LocalDate.now())) {
            warnings.put("ERR-SC02-13", "Kết luận tiêu chuẩn chính trị đã quá 6 tháng kể từ ngày ban hành (BR-SC02-02) — "
                    + "cần cập nhật kết luận mới hoặc xác nhận ngoại lệ trước khi Trình kiểm soát");
        }
    }

    private void validateAttachments(Map<String, String> errors, Map<String, String> warnings, List<String> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            errors.put("attachmentIds", "ERR-SC02-12: Hồ sơ phải có tối thiểu 1 tệp đính kèm (đề án nhân sự)");
            return;
        }
        List<Attachment> attachments = safeList(attachmentClient.findAllById(attachmentIds).getData());
        if (attachments.size() != attachmentIds.size()) {
            errors.put("attachmentIds", "ERR-SC02-12: Có attachment_id không tồn tại trong hệ thống");
        }
        warnMissingPersonnelAttachment(warnings, attachments);
    }

    /** BR-SC02-03 — cảnh báo, không chặn lưu (chỉ chặn Trình kiểm soát, ngoài phạm vi task này). */
    private void warnMissingPersonnelAttachment(Map<String, String> warnings, List<Attachment> attachments) {
        boolean hasPersonnelDoc = attachments.stream()
                .anyMatch(a -> a.getAttachmentType() != null && PERSONNEL_ATTACHMENT_TYPES.contains(a.getAttachmentType()));
        if (!hasPersonnelDoc) {
            warnings.put("ERR-SC02-14", "Hồ sơ chưa có tài liệu 'Đề án nhân sự' hoặc 'Danh sách đảng viên' (BR-SC02-03) — "
                    + "cần xác nhận trước khi Trình kiểm soát");
        }
    }

    private String generateCaseCode(String caseTypeCode) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = caseTypeCode + "-" + LocalDate.now().format(formatter) + "-"
                    + String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
            if (Boolean.FALSE.equals(caseClient.existsByCaseCode(candidate).getData())) {
                return candidate;
            }
        }
        throw new CommonException("Không sinh được case_code duy nhất, vui lòng thử lại");
    }

    private void applyUpdates(CaseEstablishment establishment, EstablishmentCaseRequest request, String organizationTypeId) {
        if (request.getBrcd() != null) {
            establishment.setBrcd(request.getBrcd());
        }
        if (request.getStaffCount() != null) {
            establishment.setStaffCount(request.getStaffCount());
        }
        if (request.getLeadershipInfo() != null) {
            establishment.setLeadershipStaffId(request.getLeadershipInfo().getStaffId());
            establishment.setLeadershipInfoText(request.getLeadershipInfo().getFreeText());
        }
        if (request.getBoardDecisionNo() != null) {
            establishment.setBoardDecisionNo(request.getBoardDecisionNo());
        }
        if (request.getBoardDecisionDate() != null) {
            establishment.setBoardDecisionDate(request.getBoardDecisionDate());
        }
        if (request.getBoardDecisionSummary() != null) {
            establishment.setBoardDecisionSummary(request.getBoardDecisionSummary());
        }
        if (organizationTypeId != null) {
            establishment.setOrganizationTypeId(organizationTypeId);
        }
        if (request.getMemberCount() != null) {
            establishment.setMemberCount(request.getMemberCount());
        }
        if (request.getCommitteeMemberCount() != null) {
            establishment.setCommitteeMemberCount(request.getCommitteeMemberCount());
        }
        if (request.getCommitteeStructure() != null) {
            establishment.setCommitteeStructure(request.getCommitteeStructure());
        }
        if (request.getPoliticalStandardConclusionNo() != null) {
            establishment.setPoliticalStandardConclusionNo(request.getPoliticalStandardConclusionNo());
        }
        if (request.getPoliticalStandardConclusionDate() != null) {
            establishment.setPoliticalStandardConclusionDate(request.getPoliticalStandardConclusionDate());
        }
    }

    /**
     * Chỉ DỰNG danh sách (không tự lưu) — CaseEstablishmentPersistService (qldv-db) sẽ gán
     * case_id cho từng dòng và ghi trong CÙNG transaction với Case/CaseEstablishment.
     */
    private List<CaseEstablishmentCommittee> buildCommitteeRows(List<ProposedCommitteeMemberRequest> members,
                                                                 Map<String, Staff> staffByCode) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<CaseEstablishmentCommittee> rows = new ArrayList<>();
        for (ProposedCommitteeMemberRequest member : members) {
            if (!staffByCode.containsKey(member.getStaffId())) {
                continue;
            }
            rows.add(CaseEstablishmentCommittee.builder()
                    .staffCode(member.getStaffId())
                    .proposedPosition(ECommitteePosition.valueOf(member.getProposedPosition()).getId())
                    .build());
        }
        return rows;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
