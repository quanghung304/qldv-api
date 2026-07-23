package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardReviewRequest {
    Integer method;
    String boardDocumentNo;
    LocalDate boardDocumentDate;
    Integer ballotsIssued;
    Integer ballotsReturned;
    Integer ballotsAgree;
    Integer ballotsDisagree;
    String opinionNotes;

    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();

        if (!EBoardReviewMethod.isValid(method)) {
            errors.put("method", "ERR-SC04-01: method bắt buộc và chỉ nhận giá trị 1 hoặc 2");
        } else if (EBoardReviewMethod.MEETING.matches(method)) {
            if (boardDocumentNo == null || boardDocumentNo.isBlank() || boardDocumentDate == null) {
                errors.put("board_document", "ERR-SC04-02: board_document_no và board_document_date là bắt buộc");
            }

            if (boardDocumentDate != null && boardDocumentDate.isAfter(LocalDate.now())) {
                errors.put("board_document_date", "ERR-SC04-03: board_document_date không được sau ngày hiện tại");
            }
        } else {
            if (ballotsIssued == null || ballotsIssued <= 0) {
                errors.put("ballots_issued", "ERR-SC04-04: ballots_issued phải lớn hơn 0");
            }

            if (ballotsReturned != null && ballotsIssued != null && ballotsReturned > ballotsIssued) {
                errors.put("ballots_returned", "ERR-SC04-05: ballots_returned không được vượt quá ballots_issued");
            }

            int agree = ballotsAgree == null ? 0 : ballotsAgree;
            int disagree = ballotsDisagree == null ? 0 : ballotsDisagree;
            if (ballotsReturned != null && agree + disagree > ballotsReturned) {
                errors.put("ballots", "ERR-SC04-06: tổng phiếu đồng ý và không đồng ý không được vượt quá ballots_returned");
            }
        }
        if (!errors.isEmpty()) throw new FieldValidationException(errors);
    }
}
