package com.agribank.qldv_api.response.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserListResponse {
    String userId;
    String fullName;
    String username;
    Integer brcd;
    String branchName;
    String roleId;
    String roleName;
    Integer accountStatus;
    Timestamp createdAt;
}
