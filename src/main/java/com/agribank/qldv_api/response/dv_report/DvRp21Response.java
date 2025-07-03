package com.agribank.qldv_api.response.dv_report;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DvRp21Response {
    String organizationCode;
    String organizationGroupBName;
    String organizationGroupCName;
    String staffCode;
    String fullName;
    Date birthDay;
    String mainJob;
    String recruitBrcd;
    Date admissionDate;
    Date recognitionDeadline;
    String skillLevel;
    String partyMemberLevel;
    Integer newPartyMemberClass;
}
