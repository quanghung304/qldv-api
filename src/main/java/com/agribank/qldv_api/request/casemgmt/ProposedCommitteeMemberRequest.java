package com.agribank.qldv_api.request.casemgmt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/** 1 dòng trong danh sách cấp ủy dự kiến (field 12, SC-02) — proposedPosition ∈ ECommitteePosition. */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProposedCommitteeMemberRequest {
    String staffId;
    String proposedPosition;
}
