package com.agribank.qldv_api.request.organizationTransform;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationTransformRequest {
    String organizationCode;
    String oldName;
    String oldForm;
    String newName;
    String newForm;
    String decisionCommittee;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
    String status;
}
