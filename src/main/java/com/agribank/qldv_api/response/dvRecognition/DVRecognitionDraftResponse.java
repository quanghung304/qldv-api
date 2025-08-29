package com.agribank.qldv_api.response.dvRecognition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class DVRecognitionDraftResponse {
    String id;
    String staffCode;
    String staffName;
    String conclusionNumber;
    String decisionNumber;
    Date conclusionDate;
    Date decisionDate;
    Date effectiveDate;
    Timestamp updatedAt;
    String createdBy;
    String approvedBy;
    Integer status;
}
