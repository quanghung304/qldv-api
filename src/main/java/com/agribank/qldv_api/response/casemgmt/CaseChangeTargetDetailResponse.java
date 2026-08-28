package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/** 1 TCĐ đích của hồ sơ biến động (Sáp nhập/Hợp nhất=1 dòng, Chia tách=nhiều dòng), hiển thị ở GET /cases/{id}/change. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeTargetDetailResponse {
    String id;
    String organizationName;
    String organizationTypeId;
    String organizationTypeName;
    Integer memberCount;
    List<ProposedCommitteeMemberDetailResponse> committee;
}
