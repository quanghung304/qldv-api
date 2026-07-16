package com.agribank.qldv_api.response.role;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleDtoResponse {
    String userId;
    String roleId;
    String roleCode;
    String roleName;
}
