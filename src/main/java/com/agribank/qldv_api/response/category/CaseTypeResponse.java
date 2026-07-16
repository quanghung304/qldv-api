package com.agribank.qldv_api.response.category;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseTypeResponse {
    String id;
    String code;
    String name;
    Integer minOrganizationCount;
    Integer maxOrganizationCount;
}
