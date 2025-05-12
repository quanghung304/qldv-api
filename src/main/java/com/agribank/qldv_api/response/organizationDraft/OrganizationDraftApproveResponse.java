package com.agribank.qldv_api.response.organizationDraft;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDraftApproveResponse {
    String id;
    String approve;
    String message;
}
