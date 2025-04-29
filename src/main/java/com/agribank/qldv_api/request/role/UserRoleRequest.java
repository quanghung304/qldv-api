package com.agribank.qldv_api.request.role;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleRequest {
    String userId;
    List<String> roleIds;
}