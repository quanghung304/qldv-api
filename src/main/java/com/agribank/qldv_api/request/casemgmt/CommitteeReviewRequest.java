package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API-SC05-01 (PUT /cases/{id}/establishment/committee-review) — Bước 3 giai đoạn 1, ghi nhận ý
 * kiến Ban Chấp hành (BCH) Đảng bộ Agribank. Cấu trúc/validate mirror nguyên
 * {@code BoardReviewRequest} (S3-01/API-SC04-01) theo đúng chỉ định "giống hệt API-SC04-01" của
 * prompt_S3-02 mục 3 — chỉ đổi tên field theo đề xuất GC-S3-02-02 (prefix committee_ thay
 * board_). Phần validate ở đây KHÔNG cần DB (format/độ dài/khoảng giá trị) — validate cần DB
 * (mốc so ngày với Bước 2) nằm ở {@code CommitteeReviewService}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitteeReviewRequest {
    Integer committeeMethod;
    String committeeDocumentNo;
    LocalDate committeeDocumentDate;
    Integer ballotsIssued;
    Integer ballotsReturned;
    Integer ballotsAgree;
    Integer ballotsDisagree;
    String opinionNotes;

    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();

        if (!EBoardReviewMethod.isValid(committeeMethod)) {
            errors.put("committeeMethod", "ERR-SC04-01: committee_method bắt buộc và chỉ nhận giá trị MEETING/BALLOT");
        } else if (EBoardReviewMethod.MEETING.matches(committeeMethod)) {
            validateMeetingFields(errors);
        } else {
            validateBallotFields(errors);
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    private void validateMeetingFields(Map<String, String> errors) {
        if (committeeDocumentNo == null || committeeDocumentNo.isBlank() || committeeDocumentNo.length() > 100) {
            errors.put("committeeDocumentNo", "ERR-SC04-02: committee_document_no không được để trống, không vượt quá 100 ký tự");
        }
        if (committeeDocumentDate == null) {
            errors.put("committeeDocumentDate", "ERR-SC04-02: committee_document_date là bắt buộc");
        } else if (committeeDocumentDate.isAfter(LocalDate.now())) {
            errors.put("committeeDocumentDate", "ERR-SC04-03: committee_document_date không được sau ngày hiện tại");
        }
    }

    private void validateBallotFields(Map<String, String> errors) {
        if (ballotsIssued == null || ballotsIssued <= 0) {
            errors.put("ballotsIssued", "ERR-SC04-04: ballots_issued phải lớn hơn 0");
        }
        if (ballotsReturned != null && (ballotsReturned < 0 || (ballotsIssued != null && ballotsReturned > ballotsIssued))) {
            errors.put("ballotsReturned", "ERR-SC04-05: ballots_returned phải >= 0 và không được vượt quá ballots_issued");
        }
        int agree = ballotsAgree == null ? 0 : ballotsAgree;
        int disagree = ballotsDisagree == null ? 0 : ballotsDisagree;
        if (agree < 0 || disagree < 0) {
            errors.put("ballots", "ERR-SC04-06: ballots_agree/ballots_disagree không được âm");
        } else if (ballotsReturned != null && agree + disagree > ballotsReturned) {
            errors.put("ballots", "ERR-SC04-06: tổng phiếu đồng ý và không đồng ý không được vượt quá ballots_returned");
        }
        if (opinionNotes != null && opinionNotes.length() > 1000) {
            errors.put("opinionNotes", "ERR-SC02-01: opinion_notes không được vượt quá 1000 ký tự");
        }
    }
}
