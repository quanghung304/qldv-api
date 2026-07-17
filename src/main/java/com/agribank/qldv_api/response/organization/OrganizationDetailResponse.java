package com.agribank.qldv_api.response.organization;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDetailResponse extends OrganizationListItemResponse {
    ParentOrganizationResponse parentOrganization;
}
