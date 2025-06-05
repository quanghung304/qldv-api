package com.agribank.qldv_api.response.organization_draft;

import com.agribank.qldv_api.response.organization.OrganizationResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDraftResponse extends OrganizationResponse {
    String id;
    Integer approve;
    String usernameCreated;
    Integer userBrcdCreated;
    String usernameAccepted;
    Integer userBrcdAccepted;
    String organizationCode;
}
