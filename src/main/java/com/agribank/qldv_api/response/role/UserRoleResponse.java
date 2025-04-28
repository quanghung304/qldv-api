package com.agribank.qldv_api.response.role;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleResponse {
    private String userId;
    private String roleId;
}