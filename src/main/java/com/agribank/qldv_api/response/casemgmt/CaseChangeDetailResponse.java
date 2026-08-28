package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /cases/{id}/change — chi tiết đầy đủ Bước 1 SC-08 (PMDV_CASE + PMDV_CASE_CHANGE + TCĐ liên
 * quan + TCĐ đích/cấp ủy dự kiến từng đích), phục vụ màn hình xem/sửa hồ sơ biến động TCĐ (Giải
 * thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên) — cùng pattern với
 * {@code EstablishmentCaseDetailResponse}. Field nào thuộc CaseChange mà hồ sơ chưa từng lưu
 * (chưa gọi API-SC08-01/02 lần nào, trường hợp hiếm) trả về null thay vì lỗi 404.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeDetailResponse {
    // Thông tin chung của hồ sơ (PMDV_CASE)
    String caseId;
    String caseCode;
    String caseTypeId;
    String caseTypeName;
    String statusId;
    String statusName;
    Integer authorityLevel;
    String originFlow;
    String createdBy;
    String createdByName;
    Timestamp createdAt;
    Timestamp updatedAt;
    Timestamp completedAt;

    // Field Bước 1 SC-08 (PMDV_CASE_CHANGE + TCĐ liên quan/đích)
    String proposedOrganizationName;
    /** TCĐ NGUỒN liên quan tới hồ sơ (PMDV_CASE_ORGANIZATION, link_role=SOURCE) — kể cả survivorOrganizationId (Sáp nhập). */
    List<CaseOrganizationSummaryResponse> organizations;
    String survivorOrganizationId;
    String boardDecisionNo;
    LocalDate boardDecisionDate;
    Integer affectedMemberCount;
    Integer affectedCommitteeMemberCount;
    /** Sáp nhập/Hợp nhất=1 phần tử, Chia tách≥2 phần tử, Giải thể/Đổi tên=rỗng. */
    List<CaseChangeTargetDetailResponse> targets;
}
