package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.FieldValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Body dùng chung cho API-SC08-01 (POST /cases/changes — tạo mới) và API-SC08-02
 * (PUT /cases/{id}/change — cập nhật một phần, field null = không đổi) — Bước 1 SC-08 của 5
 * nghiệp vụ biến động TCĐ (Giải thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên), field hiển thị/bắt buộc
 * ĐỘNG theo case_type nên chỉ validate FORMAT (không cần DB) ở đây — cùng convention với
 * {@code EstablishmentCaseRequest}. Phần validate CẦN DB (case_type có tồn tại/code là gì, ràng
 * buộc số lượng theo BR-SC08-01, TCĐ có tồn tại/đang Hoạt động, trùng tên...) nằm ở
 * {@code CaseChangeService}.
 *
 * {@code caseTypeId} chỉ được dùng ở API-SC08-01 (BẮT BUỘC) — API-SC08-02 bỏ qua field này dù
 * client có gửi lên (loại nghiệp vụ cố định từ lúc tạo hồ sơ, không đổi được, xem prompt_S4-01).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeRequest {
    private static final int MAX_PROPOSED_TARGET_NAME_LENGTH = 250;
    private static final int MAX_BOARD_DECISION_NO_LENGTH = 100;

    String caseTypeId;
    List<String> organizationIds;
    String survivorOrganizationId;
    String proposedTargetName;
    String boardDecisionNo;
    LocalDate boardDecisionDate;

    /** API-SC08-01 (tạo mới) — caseTypeId/organizationIds/boardDecisionNo/boardDecisionDate bắt buộc có giá trị. */
    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        requireText(errors, "caseTypeId", caseTypeId, Integer.MAX_VALUE,
                "ERR-SC08-01: caseTypeId không được để trống");
        validateOrganizationIdsFormat(errors, true);
        validateSurvivorOrganizationIdFormat(errors);
        validateProposedTargetNameFormat(errors);
        requireText(errors, "boardDecisionNo", boardDecisionNo, MAX_BOARD_DECISION_NO_LENGTH,
                "ERR-SC08-01: boardDecisionNo không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        requirePastOrPresentDate(errors, "boardDecisionDate", boardDecisionDate,
                "ERR-SC08-01: boardDecisionDate không được để trống và không được sau ngày hiện tại");
        throwIfInvalid(errors);
    }

    /**
     * API-SC08-02 (cập nhật một phần) — chỉ validate format field nào THỰC SỰ được gửi lên
     * (null = không đổi). caseTypeId KHÔNG được validate/sử dụng ở đây (bất biến sau khi tạo).
     */
    public void validateForUpdate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (organizationIds != null) {
            validateOrganizationIdsFormat(errors, false);
        }
        if (survivorOrganizationId != null) {
            validateSurvivorOrganizationIdFormat(errors);
        }
        if (proposedTargetName != null) {
            validateProposedTargetNameFormat(errors);
        }
        if (boardDecisionNo != null) {
            requireText(errors, "boardDecisionNo", boardDecisionNo, MAX_BOARD_DECISION_NO_LENGTH,
                    "ERR-SC08-01: boardDecisionNo không được để trống, không vượt quá " + MAX_BOARD_DECISION_NO_LENGTH + " ký tự");
        }
        if (boardDecisionDate != null) {
            requirePastOrPresentDate(errors, "boardDecisionDate", boardDecisionDate,
                    "ERR-SC08-01: boardDecisionDate không được sau ngày hiện tại");
        }
        throwIfInvalid(errors);
    }

    /**
     * Chỉ kiểm tra format tự thân (không rỗng, không trùng lặp) — ràng buộc SỐ LƯỢNG theo đúng
     * case_type (BR-SC08-01, lookup PMDV_CASE_TYPE.min/max_organization_count) nằm ở service vì
     * cần DB (coding-convention.md mục 11 — không hardcode ngưỡng theo biến thể trong code).
     */
    private void validateOrganizationIdsFormat(Map<String, String> errors, boolean required) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            if (required) {
                errors.put("organizationIds", "ERR-SC08-01: organizationIds không được để trống");
            }
            return;
        }
        boolean hasBlank = organizationIds.stream().anyMatch(id -> id == null || id.isBlank());
        if (hasBlank) {
            errors.put("organizationIds", "ERR-SC08-01: organizationIds không được chứa giá trị rỗng");
            return;
        }
        long distinctCount = organizationIds.stream().distinct().count();
        if (distinctCount != organizationIds.size()) {
            errors.put("organizationIds", "ERR-SC08-01: organizationIds không được chứa TCĐ trùng lặp");
        }
    }

    private void validateSurvivorOrganizationIdFormat(Map<String, String> errors) {
        if (survivorOrganizationId != null && survivorOrganizationId.isBlank()) {
            errors.put("survivorOrganizationId", "ERR-SC08-05: survivorOrganizationId không được là chuỗi rỗng");
        }
    }

    private void validateProposedTargetNameFormat(Map<String, String> errors) {
        if (proposedTargetName != null && proposedTargetName.length() > MAX_PROPOSED_TARGET_NAME_LENGTH) {
            errors.put("proposedTargetName", "ERR-SC08-03: proposedTargetName không được vượt quá "
                    + MAX_PROPOSED_TARGET_NAME_LENGTH + " ký tự");
        }
    }

    private void requireText(Map<String, String> errors, String field, String value, int maxLength, String message) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            errors.put(field, message);
        }
    }

    private void requirePastOrPresentDate(Map<String, String> errors, String field, LocalDate value, String message) {
        if (value == null || value.isAfter(LocalDate.now())) {
            errors.put(field, message);
        }
    }

    private void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
