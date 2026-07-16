package com.agribank.qldv_api.response.category;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatusResponse {
    String id;
    String statusCode;
    String statusName;
    String flowCode;
    Integer statusType;
    String statusTypeLabel;
    Boolean isLocked;
}
