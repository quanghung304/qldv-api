package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ADResponse {
    String fullName;
    String email;
}
