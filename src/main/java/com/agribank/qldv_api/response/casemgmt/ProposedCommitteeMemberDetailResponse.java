package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/** 1 dòng cấp ủy dự kiến hiển thị ở GET /cases/{id}/establishment — staffName resolve qua EmployeeInfoClient (GA). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProposedCommitteeMemberDetailResponse {
    String staffId;
    String staffName;
    String proposedPosition;
}
