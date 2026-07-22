package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.FieldValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Body dùng chung cho API-SC02-01 (POST /cases/establishments — tạo mới, mọi field B phải có)
 * và API-SC02-02 (PUT /cases/{id}/establishment — cập nhật một phần, field null = không đổi).
 * 15 field Bước 1 SC-02 (FSD mục 3.2) — ánh xạ trực tiếp, JSON camelCase theo đúng quy ước hiện
 * có của repo (api-conventions.md, đối chiếu các request khác trong qldv-api).
 *
 * {@link #validate()}/{@link #validateForUpdate()} chỉ gồm phần KHÔNG cần truy vấn DB (format,
 * độ dài, khoảng giá trị, cross-field tự thân trong request) — gọi ở controller TRƯỚC khi vào
 * service (cùng convention với {@code CaseSearchRequest}/{@code WorkflowActionRequest}). Phần
 * validate CẦN DB (tồn tại staff_id/organization_type_id, trùng tên tổ chức, ngưỡng member_count
 * theo danh mục...) vẫn nằm ở {@code EstablishmentCaseService}.
 *
 * organizationTypeId (field 8) bị BỎ QUA khi service nhận allowedOrganizationTypeId khác null
 * (BR-SC07-01, dùng lại nguyên vẹn cho SC-07/Sprint 5) — client không thể override trong
 * trường hợp đó dù có gửi field này lên.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentCaseRequest {
    private static final int MAX_STAFF_COUNT = 100000;
    private static final int MAX_LEADERSHIP_FREE_TEXT_LENGTH = 500;
    private static final int MAX_BOARD_DECISION_NO_LENGTH = 100;
    private static final int MAX_BOARD_DECISION_SUMMARY_LENGTH = 1000;
    private static final int MAX_ORGANIZATION_NAME_LENGTH = 250;
    private static final int MAX_COMMITTEE_STRUCTURE_LENGTH = 500;
    private static final int MAX_CONCLUSION_NO_LENGTH = 50;
    private static final long POLITICAL_STANDARD_VALIDITY_MONTHS = 6;
    private static final Set<String> VALID_POSITIONS = Set.of("SECRETARY", "DEPUTY_SECRETARY", "MEMBER");

    Integer brcd;
    Integer staffCount;
    LeadershipInfoRequest leadershipInfo;
    String boardDecisionNo;
    LocalDate boardDecisionDate;
    String boardDecisionSummary;
    String proposedOrganizationName;
    String organizationTypeId;
    Integer memberCount;
    Integer committeeMemberCount;
    String committeeStructure;
    List<ProposedCommitteeMemberRequest> proposedCommitteeMembers;
    String politicalStandardConclusionNo;
    LocalDate politicalStandardConclusionDate;

    /** API-SC02-01 (tạo mới) — toàn bộ field B phải có giá trị hợp lệ. */
    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        requirePositiveInt(errors, "brcd", brcd, null, "ERR-SC02-01: brcd (mã chi nhánh) không được để trống");
        requirePositiveInt(errors, "staffCount", staffCount, MAX_STAFF_COUNT,
                "ERR-SC02-02: staff_count phải là số nguyên dương, tối đa " + MAX_STAFF_COUNT);
        validateLeadershipInfoFormat(errors);
        requireText(errors, "boardDecisionNo", boardDecisionNo, MAX_BOARD_DECISION_NO_LENGTH,
                "ERR-SC02-01: board_decision_no không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "boardDecisionDate", boardDecisionDate,
                "ERR-SC02-04: board_decision_date không được để trống và không được sau ngày hiện tại");
        requireText(errors, "boardDecisionSummary", boardDecisionSummary, MAX_BOARD_DECISION_SUMMARY_LENGTH,
                "ERR-SC02-01: board_decision_summary không được để trống, không vượt quá " + MAX_BOARD_DECISION_SUMMARY_LENGTH + " ký tự");
        requireText(errors, "proposedOrganizationName", proposedOrganizationName, MAX_ORGANIZATION_NAME_LENGTH,
                "ERR-SC02-01: proposed_organization_name không được để trống, không vượt quá " + MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
        requirePositiveInt(errors, "memberCount", memberCount, null, null);
        validateCommitteeMemberCountFormat(errors, committeeMemberCount, memberCount);
        requireText(errors, "committeeStructure", committeeStructure, MAX_COMMITTEE_STRUCTURE_LENGTH,
                "ERR-SC02-01: committee_structure không được để trống, không vượt quá " + MAX_COMMITTEE_STRUCTURE_LENGTH + " ký tự");
        validateProposedCommitteeMembersFormat(errors, committeeMemberCount);
        requireText(errors, "politicalStandardConclusionNo", politicalStandardConclusionNo, MAX_CONCLUSION_NO_LENGTH,
                "ERR-SC02-01: political_standard_conclusion_no không được để trống, không vượt quá " + MAX_CONCLUSION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "politicalStandardConclusionDate", politicalStandardConclusionDate,
                "ERR-SC02-04: political_standard_conclusion_date không được để trống và không được sau ngày hiện tại");
        validatePoliticalStandardValidity(errors, politicalStandardConclusionDate);
        throwIfInvalid(errors);
    }

    /**
     * API-SC02-02 (cập nhật một phần) — chỉ validate format field nào THỰC SỰ được gửi lên
     * (null = không đổi, bỏ qua). Cross-field (VD committee_member_count so với member_count)
     * chỉ tự kiểm được ở đây khi CẢ 2 field cùng có mặt trong request này; nếu 1 trong 2 field
     * không gửi lên, việc so khớp với giá trị đang lưu trong DB do service đảm nhiệm.
     */
    public void validateForUpdate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (brcd != null) {
            requirePositiveInt(errors, "brcd", brcd, null, "ERR-SC02-01: brcd (mã chi nhánh) không được để trống");
        }
        if (staffCount != null) {
            requirePositiveInt(errors, "staffCount", staffCount, MAX_STAFF_COUNT,
                    "ERR-SC02-02: staff_count phải là số nguyên dương, tối đa " + MAX_STAFF_COUNT);
        }
        if (leadershipInfo != null) {
            validateLeadershipInfoFormat(errors);
        }
        if (boardDecisionNo != null) {
            requireText(errors, "boardDecisionNo", boardDecisionNo, MAX_BOARD_DECISION_NO_LENGTH,
                    "ERR-SC02-01: board_decision_no không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        }
        if (boardDecisionDate != null) {
            requirePastOrPresentDate(errors, "boardDecisionDate", boardDecisionDate,
                    "ERR-SC02-04: board_decision_date không được sau ngày hiện tại");
        }
        if (boardDecisionSummary != null) {
            requireText(errors, "boardDecisionSummary", boardDecisionSummary, MAX_BOARD_DECISION_SUMMARY_LENGTH,
                    "ERR-SC02-01: board_decision_summary không được để trống, không vượt quá " + MAX_BOARD_DECISION_SUMMARY_LENGTH + " ký tự");
        }
        if (proposedOrganizationName != null) {
            requireText(errors, "proposedOrganizationName", proposedOrganizationName, MAX_ORGANIZATION_NAME_LENGTH,
                    "ERR-SC02-01: proposed_organization_name không được để trống, không vượt quá " + MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
        }
        if (memberCount != null) {
            requirePositiveInt(errors, "memberCount", memberCount, null, null);
        }
        if (committeeMemberCount != null) {
            validateCommitteeMemberCountFormat(errors, committeeMemberCount, memberCount);
        }
        if (committeeStructure != null) {
            requireText(errors, "committeeStructure", committeeStructure, MAX_COMMITTEE_STRUCTURE_LENGTH,
                    "ERR-SC02-01: committee_structure không được để trống, không vượt quá " + MAX_COMMITTEE_STRUCTURE_LENGTH + " ký tự");
        }
        if (proposedCommitteeMembers != null) {
            validateProposedCommitteeMembersFormat(errors, committeeMemberCount);
        }
        if (politicalStandardConclusionNo != null) {
            requireText(errors, "politicalStandardConclusionNo", politicalStandardConclusionNo, MAX_CONCLUSION_NO_LENGTH,
                    "ERR-SC02-01: political_standard_conclusion_no không được để trống, không vượt quá " + MAX_CONCLUSION_NO_LENGTH + " ký tự");
        }
        if (politicalStandardConclusionDate != null) {
            requirePastOrPresentDate(errors, "politicalStandardConclusionDate", politicalStandardConclusionDate,
                    "ERR-SC02-04: political_standard_conclusion_date không được sau ngày hiện tại");
        }
        throwIfInvalid(errors);
    }

    private void validateLeadershipInfoFormat(Map<String, String> errors) {
        if (leadershipInfo == null) {
            return;
        }
        boolean hasStaffId = leadershipInfo.getStaffId() != null && !leadershipInfo.getStaffId().isBlank();
        if (!hasStaffId && leadershipInfo.getFreeText() != null
                && leadershipInfo.getFreeText().length() > MAX_LEADERSHIP_FREE_TEXT_LENGTH) {
            errors.put("leadershipInfo", "ERR-SC02-03: leadership_info nhập tay không được vượt quá "
                    + MAX_LEADERSHIP_FREE_TEXT_LENGTH + " ký tự");
        }
        // staff_id có tồn tại ở hệ thống GA hay không cần gọi EmployeeInfoClient — do service xử lý.
    }

    private void validateCommitteeMemberCountFormat(Map<String, String> errors, Integer committeeMemberCount, Integer memberCount) {
        if (committeeMemberCount == null || committeeMemberCount < 0) {
            errors.put("committeeMemberCount", "ERR-SC02-08: committee_member_count phải >= 0");
            return;
        }
        if (memberCount != null && committeeMemberCount > memberCount) {
            errors.put("committeeMemberCount", "ERR-SC02-08: committee_member_count không được vượt quá member_count");
        }
    }

    private void validateProposedCommitteeMembersFormat(Map<String, String> errors, Integer committeeMemberCount) {
        if (proposedCommitteeMembers == null || proposedCommitteeMembers.isEmpty()) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: proposed_committee_members không được để trống");
            return;
        }
        if (committeeMemberCount != null && proposedCommitteeMembers.size() != committeeMemberCount) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: Số dòng proposed_committee_members phải bằng committee_member_count");
        }
        long secretaryCount = proposedCommitteeMembers.stream().filter(m -> "SECRETARY".equals(m.getProposedPosition())).count();
        if (secretaryCount > 1) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: Danh sách cấp ủy dự kiến chỉ được tối đa 1 SECRETARY");
        }
        boolean invalidPosition = proposedCommitteeMembers.stream()
                .anyMatch(m -> m.getProposedPosition() == null || !VALID_POSITIONS.contains(m.getProposedPosition()));
        if (invalidPosition) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-09: proposed_position phải thuộc SECRETARY/DEPUTY_SECRETARY/MEMBER");
        }
        // staff_id tồn tại/hợp lệ (ERR-SC02-10) cần gọi EmployeeInfoClient — do service xử lý.
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

    /** BR-SC02-02 — cảnh báo (không chặn lưu), quá 6 tháng kể từ ngày ban hành kết luận TCCT. */
    private void validatePoliticalStandardValidity(Map<String, String> errors, LocalDate conclusionDate) {
        if (conclusionDate != null && conclusionDate.plusMonths(POLITICAL_STANDARD_VALIDITY_MONTHS).isBefore(LocalDate.now())) {
            errors.put("ERR-SC02-13", "Kết luận tiêu chuẩn chính trị đã quá 6 tháng kể từ ngày ban hành (BR-SC02-02) — "
                    + "cần cập nhật kết luận mới hoặc xác nhận ngoại lệ trước khi Trình kiểm soát");
        }
    }

    private void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
