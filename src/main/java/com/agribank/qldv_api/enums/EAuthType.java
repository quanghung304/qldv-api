package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EAuthType {
    SSO_EMAIL,
    LOCAL_PASSWORD;
}
