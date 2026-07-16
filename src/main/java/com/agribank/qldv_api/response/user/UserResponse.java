package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String staffCode;
    Integer idIam;
    String username;
    String fullName;
    Integer brcd;
    Integer depId;
    String accountStatus;
    Integer deleted;
    String branchName;
    List<String> roles;
    List<String> roleIds;
    String organizationCode;
    String createdBy;
}
