package com.agribank.qldv_api.response.establishment_dissolve_draft;

import com.agribank.qldv_api.response.establishment_dissolve.EstablishmentDissolveResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EDDraftResponse extends EstablishmentDissolveResponse {
    String usernameCreated;
    Integer userBrcdCreated;
    String usernameAccepted;
    Integer userBrcdAccepted;
    String organizationCode;
    Integer status;
}
