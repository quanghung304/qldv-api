package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String dvCode;
    private Integer idIam;
    private Integer roleId;
    private String username;
    private String fullName;
    private String email;
    private Integer brcd;
    private Integer depId;
    private String phone;
    private Integer vneid;
}
