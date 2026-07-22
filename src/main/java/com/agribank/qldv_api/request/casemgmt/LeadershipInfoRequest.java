package com.agribank.qldv_api.request.casemgmt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Field 3 (leadership_info, SC-02) — hoặc tra cứu hệ thống GA theo staffId, hoặc nhập tay
 * (freeText). Đúng 1 trong 2 được cung cấp — validate ở service (ERR-SC02-03).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeadershipInfoRequest {
    String staffId;
    String freeText;
}
