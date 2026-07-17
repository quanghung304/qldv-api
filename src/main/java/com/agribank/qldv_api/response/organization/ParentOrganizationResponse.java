package com.agribank.qldv_api.response.organization;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParentOrganizationResponse {
    String id;
    String organizationName;
}
