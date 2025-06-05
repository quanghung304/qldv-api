package com.agribank.qldv_api.response.develop_plan;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevelopPlanResponse {
    String id;
    String organizationCode;
    String name;
    Integer start;
    Integer end;
    Integer target;
    String prntCode;
    Integer hasChild;
    Date createdAt;
    Date updatedAt;
}
