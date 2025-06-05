package com.agribank.qldv_api.response.organization_reference;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationReferenceResponse {
    String code;
    String name;
}
