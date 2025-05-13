package com.agribank.qldv_api.request.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequestedUpdate {
    String vneid;
    String phone;
    String fullName;
}
