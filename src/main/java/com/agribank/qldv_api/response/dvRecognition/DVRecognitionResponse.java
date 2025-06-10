package com.agribank.qldv_api.response.dvRecognition;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVRecognitionResponse {
    String id;
    String dvCode;
    String dvName;
    //So KL/Nghi Quyet
    String conclusionNumber;
    //Số QD
    String decisionNumber;
    //Ngay KL/Nghi Quyet
    Date conclusionDate;
    //Ngay QD
    Date decisionDate;
}
