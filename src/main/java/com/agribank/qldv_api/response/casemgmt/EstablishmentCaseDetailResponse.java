package com.agribank.qldv_api.response.casemgmt;

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
 * GET /cases/{id}/establishment — chi tiết đầy đủ 15 field Bước 1 SC-02 (PMDV_CASE +
 * PMDV_CASE_ESTABLISHMENT + danh sách cấp ủy dự kiến), phục vụ màn hình xem/sửa (SC-01, SC-02).
 * Field nào thuộc CaseEstablishment mà hồ sơ chưa từng lưu (chưa gọi API-SC02-01/02 lần nào,
 * trường hợp hiếm) sẽ trả về null thay vì lỗi 404.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentCaseDetailResponse {
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

    // 15 field Bước 1 SC-02
    String proposedOrganizationName;
    Integer brcd;
    Integer staffCount;
    String leadershipStaffId;
    String leadershipStaffName;
    String leadershipInfoText;
    String boardDecisionNo;
    LocalDate boardDecisionDate;
    String boardDecisionSummary;
    String organizationTypeId;
    String organizationTypeName;
    Integer memberCount;
    Integer committeeMemberCount;
    String committeeStructure;
    List<ProposedCommitteeMemberDetailResponse> proposedCommitteeMembers;
    String politicalStandardConclusionNo;
    LocalDate politicalStandardConclusionDate;
}
