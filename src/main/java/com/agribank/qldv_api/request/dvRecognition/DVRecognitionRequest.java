package com.agribank.qldv_api.request.dvRecognition;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVRecognitionRequest {
    String id;
    String dvCode;
    String dvName;
    String conclusionNumber;
    String decisionNumber;
    Date conclusionDate;
    Date decisionDate;
    Date effectiveDate;
}
