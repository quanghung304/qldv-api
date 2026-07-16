package com.agribank.qldv_api.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserListResponse {
    @JsonProperty("user_id")
    String userId;

    @JsonProperty("full_name")
    String fullName;

    String username;

    @JsonProperty("unit_id")
    Integer unitId;

    @JsonProperty("unit_name")
    String unitName;

    @JsonProperty("role_id")
    String roleId;

    @JsonProperty("role_name")
    String roleName;

    @JsonProperty("account_status")
    String accountStatus;

    @JsonProperty("created_at")
    Timestamp createdAt;
}
