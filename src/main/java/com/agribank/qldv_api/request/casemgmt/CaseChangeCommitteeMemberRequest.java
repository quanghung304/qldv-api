package com.agribank.qldv_api.request.casemgmt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/** 1 dòng cấp ủy dự kiến của 1 {@code CaseChangeTargetRequest} — position ∈ ECommitteePosition. */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeCommitteeMemberRequest {
    String staffCode;
    String position;
}
