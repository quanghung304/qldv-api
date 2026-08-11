package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.enums.Constants;
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
 * Body của API-SC07 (POST /cases/grassroots-establishments) — SUBSET của
 * {@link EstablishmentCaseRequest}: BỎ organizationTypeId (server gán cứng CBTT_DUCS, xem
 * {@code GrassrootsEstablishmentCaseService}), BỎ leadershipInfo (SC-07 không có field "Thông
 * tin ban lãnh đạo"), THÊM {@code flowType} ("B"/"C" — do CHƯA có quy tắc tự động chọn luồng,
 * nhận trực tiếp từ client theo đúng quyết định đã xác nhận, prompt_S5-01 mục "Phần 2").
 *
 * {@link #validate()} chỉ gồm phần KHÔNG cần DB, cùng convention với
 * {@code EstablishmentCaseRequest#validate()} — TÁI SỬ DỤNG NGUYÊN HÀM (không viết lại)
 * {@link EstablishmentCaseRequest#validateCommitteeMemberCountFormat} (BR-SC02-08) và
 * {@link EstablishmentCaseRequest#validateProposedCommitteeMembersFormat} (BR-SC02-09/10) —
 * 2 hàm đó đã được nới quyền truy cập (package-private static) đúng cho mục đích này.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrassrootsEstablishmentCaseRequest {
    private static final Set<String> VALID_FLOW_TYPES = Set.of("B", "C");

    /** "B" hoặc "C" — Luồng B/C, nhận trực tiếp từ client (không suy luận, xem class javadoc). */
    String flowType;
    Integer brcd;
    Integer staffCount;
    String proposedOrganizationName;
    Integer memberCount;
    Integer committeeMemberCount;
    String boardDecisionNo;
    LocalDate boardDecisionDate;
    String boardDecisionSummary;
    List<ProposedCommitteeMemberRequest> proposedCommitteeMembers;
    String politicalStandardConclusionNo;
    LocalDate politicalStandardConclusionDate;

    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (flowType == null || !VALID_FLOW_TYPES.contains(flowType)) {
            errors.put("flowType", "flowType phải là 1 trong: B, C");
        }
        requirePositiveInt(errors, "brcd", brcd, null, "ERR-SC02-01: brcd (mã chi nhánh) không được để trống");
        requirePositiveInt(errors, "staffCount", staffCount, Constants.MAX_STAFF_COUNT,
                "ERR-SC02-02: staff_count phải là số nguyên dương, tối đa " + Constants.MAX_STAFF_COUNT);
        requireText(errors, "boardDecisionNo", boardDecisionNo, Constants.MAX_DECISION_NO_LENGTH,
                "ERR-SC02-01: board_decision_no không được để trống, không vượt quá " + Constants.MAX_DECISION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "boardDecisionDate", boardDecisionDate,
                "ERR-SC02-04: board_decision_date không được để trống và không được sau ngày hiện tại");
        requireText(errors, "boardDecisionSummary", boardDecisionSummary, Constants.MAX_BOARD_DECISION_SUMMARY_LENGTH,
                "ERR-SC02-01: board_decision_summary không được để trống, không vượt quá " + Constants.MAX_BOARD_DECISION_SUMMARY_LENGTH + " ký tự");
        requireText(errors, "proposedOrganizationName", proposedOrganizationName, Constants.MAX_ORGANIZATION_NAME_LENGTH,
                "ERR-SC02-01: proposed_organization_name không được để trống, không vượt quá " + Constants.MAX_ORGANIZATION_NAME_LENGTH + " ký tự");
        requirePositiveInt(errors, "memberCount", memberCount, null, null);
        EstablishmentCaseRequest.validateCommitteeMemberCountFormat(errors, committeeMemberCount, memberCount);
        EstablishmentCaseRequest.validateProposedCommitteeMembersFormat(errors, proposedCommitteeMembers, committeeMemberCount);
        requireText(errors, "politicalStandardConclusionNo", politicalStandardConclusionNo, Constants.MAX_CONCLUSION_NO_LENGTH,
                "ERR-SC02-01: political_standard_conclusion_no không được để trống, không vượt quá " + Constants.MAX_CONCLUSION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "politicalStandardConclusionDate", politicalStandardConclusionDate,
                "ERR-SC02-04: political_standard_conclusion_date không được để trống và không được sau ngày hiện tại");
        validatePoliticalStandardValidity(errors, politicalStandardConclusionDate);
        throwIfInvalid(errors);
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

    /**
     * BR-SC02-02 — cùng cơ chế (và cùng hành vi hiện tại) với
     * {@code EstablishmentCaseRequest#validatePoliticalStandardValidity}: dù comment gốc ghi
     * "cảnh báo (không chặn lưu)", implementation thật gộp chung vào {@code errors} nên VẪN CHẶN
     * lưu — xem báo cáo cuối task, đây là hành vi kế thừa nguyên trạng, không phải lỗi mới.
     */
    private void validatePoliticalStandardValidity(Map<String, String> errors, LocalDate conclusionDate) {
        if (conclusionDate != null && conclusionDate.plusMonths(Constants.POLITICAL_STANDARD_VALIDITY_MONTHS).isBefore(LocalDate.now())) {
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
