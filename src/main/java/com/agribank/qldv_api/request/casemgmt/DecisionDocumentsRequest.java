package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API-SC05-02 (PUT /cases/{id}/establishment/decision-documents) — Bước 3 giai đoạn 2 (sau ban
 * hành). 3 cặp field ánh xạ vào 3 bản ghi PMDV_DOCUMENT khác nhau (xem DecisionDocumentsService).
 *
 * GC-S3-02-04 (dung hòa FSD vs Workflow SM): mỗi "bộ" (establishDecision/committeeAppointment/
 * politicalStandardConclusion) là ĐỘC LẬP — cho phép gửi 1 hoặc cả 3 bộ trong 1 lần gọi (lưu nháp
 * từng phần), validate() ở đây chỉ kiểm tra NỘI TẠI của bộ nào ĐƯỢC gửi (còn thiếu bộ khác không
 * chặn lưu). API này CHỈ lưu dữ liệu, KHÔNG tự chuyển trạng thái — sau khi đủ 3 bộ, R-CV phải tự
 * gọi {@code POST /cases/{id}/workflow-action} (SUBMIT_CONTROL) để trình kiểm soát (xem service).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DecisionDocumentsRequest {
    String establishDecisionNo;
    LocalDate establishDecisionIssueDate;
    LocalDate establishDecisionEffectiveDate;

    String committeeAppointmentDecisionNo;
    LocalDate committeeAppointmentIssueDate;
    LocalDate committeeAppointmentEffectiveDate;

    String politicalStandardConclusionNoFinal;
    LocalDate politicalStandardConclusionIssueDate;
    LocalDate politicalStandardConclusionEffectiveDate;

    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        validateGroup(errors, "establishDecisionNo", "establishDecisionIssueDate", "establishDecisionEffectiveDate",
                establishDecisionNo, establishDecisionIssueDate, establishDecisionEffectiveDate);
        validateGroup(errors, "committeeAppointmentDecisionNo", "committeeAppointmentIssueDate", "committeeAppointmentEffectiveDate",
                committeeAppointmentDecisionNo, committeeAppointmentIssueDate, committeeAppointmentEffectiveDate);
        validateGroup(errors, "politicalStandardConclusionNoFinal", "politicalStandardConclusionIssueDate", "politicalStandardConclusionEffectiveDate",
                politicalStandardConclusionNoFinal, politicalStandardConclusionIssueDate, politicalStandardConclusionEffectiveDate);
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private void validateGroup(Map<String, String> errors, String noField, String issueField, String effectiveField,
                                String no, LocalDate issueDate, LocalDate effectiveDate) {
        boolean anyProvided = no != null || issueDate != null || effectiveDate != null;
        if (!anyProvided) {
            return;
        }
        if (no == null || no.isBlank() || no.length() > Constants.MAX_DECISION_NO_LENGTH) {
            errors.put(noField, "ERR-SC05-01: " + noField + " không được để trống, không vượt quá " + Constants.MAX_DECISION_NO_LENGTH + " ký tự");
        }
        if (issueDate == null) {
            errors.put(issueField, "ERR-SC05-01: " + issueField + " là bắt buộc khi gửi bộ văn bản này");
        }
        if (effectiveDate == null) {
            errors.put(effectiveField, "ERR-SC05-01: " + effectiveField + " là bắt buộc khi gửi bộ văn bản này");
        }
        if (issueDate != null && effectiveDate != null && effectiveDate.isBefore(issueDate)) {
            errors.put(effectiveField, "ERR-SC05-02: " + effectiveField + " không được trước " + issueField + " (BR-SC05-01)");
        }
    }
}
