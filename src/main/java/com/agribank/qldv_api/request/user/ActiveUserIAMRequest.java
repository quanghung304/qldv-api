package com.agribank.qldv_api.request.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveUserIAMRequest {
    Integer userId;
    @JsonProperty("applicationId")
    Integer appId;
    @JsonProperty("activeType")
    String type;
}
