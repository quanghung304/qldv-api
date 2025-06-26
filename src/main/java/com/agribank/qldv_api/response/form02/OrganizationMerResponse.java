package com.agribank.qldv_api.response.form02;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationMerResponse {
    String id;
    String organizationCode;
    String organizationName;
    String form;
    String decisionCommittee;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
}
