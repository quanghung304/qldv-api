package com.agribank.qldv_api.response.role;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    private String id;
    private String name;
    private String description;
    private Date createdAt;
    private Date updatedAt;
}
