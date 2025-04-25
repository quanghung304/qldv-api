package com.agribank.qldv_api.request.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserIAMUpdate {
    Integer appId;
    Integer userId;
    Integer brcd;
    Integer depId;
    String fullName;
}
