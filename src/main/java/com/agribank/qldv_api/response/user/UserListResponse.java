package com.agribank.qldv_api.response.user;

import com.agribank.qldv_api.response.role.RoleResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserListResponse {
    String userId;
    String fullName;
    String username;
    Integer brcd;
    String branchName;
    List<RoleResponse> roles;
    Integer accountStatus;
    Timestamp createdAt;
}
