package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/** 1 user đủ điều kiện được gán tiếp tục xử lý hồ sơ — GET /cases/{caseId}/eligible-assignees, FE dùng để hiển thị dropdown. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EligibleAssigneeResponse {
    String userId;
    String fullName;
    String roleCode;
}
