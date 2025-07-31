package com.agribank.qldv_api.response.form02;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EDDraftResponse extends DissolveDisbandResponse {
    String usernameCreated;
    Integer userBrcdCreated;
    String usernameAccepted;
    Integer userBrcdAccepted;
    String organizationCode;
    Integer status;
}
