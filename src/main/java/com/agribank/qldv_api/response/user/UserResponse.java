package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String dvCode;
    Integer idIam;
    String username;
    String fullName;
    String email;
    Integer brcd;
    Integer depId;
    String phone;
    String vneid;
    String staffCode;
    Integer active;
    Integer deleted;
    String branchName;
    List<String> roles;
    List<String> roleIds;
    String organizationCode;
}
