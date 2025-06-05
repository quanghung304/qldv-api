package com.agribank.qldv_api.response.committee_decision;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommitteeDecisionResponse {
    String code;
    String name;
}
