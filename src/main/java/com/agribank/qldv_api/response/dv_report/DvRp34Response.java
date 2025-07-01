package com.agribank.qldv_api.response.dv_report;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DvRp34Response {
    String id;
    String organizationCode;
    String organizationGroupBName;
    String organizationGroupCName;
    String staffCode;
    String fullName;
    Date birthDay;
    String mainJob;
    String recruitBrcd;
    //Chức danh cấp ủy
    String partyCommitteeJob;
    String typeName;
    String decisionNumber;
    String reasonPartyActivityExemption;
    String reasonLeaveParty;
    String reasonRemoveNameParty;
    Date dateOfDeath;
    Date effectiveDate;
    //cán bộ thực hiện
    String implementationStaff;
    //kiểm soát viên
    String controller;
    //Lãnh đạo ban
    String boardLeader;
}
