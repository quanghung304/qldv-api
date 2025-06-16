package com.agribank.qldv_api.response.party_reinstatement;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyReinstatementResponse {
    String id;
    String organizationCode;
    String fullName;
    String staffCode;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
}
