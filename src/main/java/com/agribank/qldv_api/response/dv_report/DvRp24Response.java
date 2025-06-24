package com.agribank.qldv_api.response.dv_report;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DvRp24Response {
    String organizationCode;
    String organizationGroupBName;
    String organizationGroupCName;
    String staffCode;
    String fullName;
    Date birthDay;
    String mainJob;
    String recruitBrcd;
    Date admissionDate;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date officialRecognitionDay;
    //cán bộ thực hiện
    String implementationStaff;
    //kiểm soát viên
    String controller;
    //Lãnh đạo ban
    String boardLeader;
}
