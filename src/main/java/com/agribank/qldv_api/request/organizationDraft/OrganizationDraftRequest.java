package com.agribank.qldv_api.request.organizationDraft;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDraftRequest {
    String id;
    Integer status;
}
