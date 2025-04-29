package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String dvCode;
    Integer idIam;
    Integer roleId;
    String username;
    String fullName;
    String email;
    Integer brcd;
    Integer depId;
    String phone;
    String vneid;
    Integer active;
    Integer deleted;
}
