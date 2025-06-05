package com.agribank.qldv_api.response.develop_plan;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevelopPlanDetailResponse {
    String id;
    String refId;
    Integer target;
    Integer min;
    Integer year;
    Date createdAt;
    Date updatedAt;
}
