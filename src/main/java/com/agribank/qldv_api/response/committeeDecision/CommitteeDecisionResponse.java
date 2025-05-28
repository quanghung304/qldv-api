package com.agribank.qldv_api.response.committeeDecision;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommitteeDecisionResponse {
    String code;
    String name;
}
