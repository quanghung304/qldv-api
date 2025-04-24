package com.agribank.qldv_api.response.apiLog;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiLogResponse {
    String username;
    String email;
    String action;
    String referenceId;
    String objectReference;
    String objectName;
    String dataType;
    String description;
    Integer brcd;
}
