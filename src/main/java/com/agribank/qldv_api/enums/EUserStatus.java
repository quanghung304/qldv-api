package com.agribank.qldv_api.enums;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EUserStatus {
    INACTIVE(0),
    ACTIVE(1);
    int id;
}
