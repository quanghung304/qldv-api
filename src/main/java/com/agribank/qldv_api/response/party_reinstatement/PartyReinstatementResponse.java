package com.agribank.qldv_api.response.party_reinstatement;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyReinstatementResponse {
    String id;
    String organizationCode;
    String organizationName;
    String fullName;
    String staffCode;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
    String decisionCommittee;
}
