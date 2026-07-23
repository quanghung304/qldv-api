package com.agribank.qldv_api.response.organization;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationSubordinateResponse {
    String id;
    String organizationCode;
    String organizationName;
    String organizationTypeId;
    String organizationTypeCode;
    String organizationTypeName;
    Integer brcd;
    String parentOrganizationId;
    Integer operationStatus;
    String operationStatusName;
    Integer memberCount;
    Integer committeeMemberCount;
    Integer level;
}
