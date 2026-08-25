package com.agribank.qldv_api.request.casemgmt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * 1 TCĐ đích cho nghiệp vụ Sáp nhập/Hợp nhất (đúng 1 phần tử) hoặc Chia tách (≥2 phần tử) — nhập
 * ngay ở Bước 1 (API-SC08-01/02), dùng lại nguyên vẹn khi Hoàn thành (API-SC06-02).
 * {@code organizationTypeId} nhận qua đây vì SC-08 (Bước 1) không có field chọn loại hình tổ chức
 * cho TCĐ đích (field NOT NULL ở PMDV_ORGANIZATION).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeTargetRequest {
    String organizationName;
    String organizationTypeId;
    Integer memberCount;
    List<CaseChangeCommitteeMemberRequest> committee;
}
