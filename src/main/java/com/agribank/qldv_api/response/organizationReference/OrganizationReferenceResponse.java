package com.agribank.qldv_api.response.organizationReference;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationReferenceResponse {
    String code;
    String name;
}
