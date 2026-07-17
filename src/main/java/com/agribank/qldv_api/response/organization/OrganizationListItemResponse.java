package com.agribank.qldv_api.response.organization;

import com.agribank.qldv_api.response.category.OrganizationTypeResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationListItemResponse {
    String organizationId;
    String organizationCode;
    String organizationName;
    OrganizationTypeResponse organizationType;
    Integer brcd;
    String branchName;
    Boolean isAuthorized;
    String decisionNoDisplay;
    LocalDate decisionDateDisplay;
    Integer operationStatus;
    Integer memberCount;
    Integer committeeMemberCount;
    Integer affiliatedCellCount;
}
