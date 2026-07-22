package com.agribank.qldv_api.request.casemgmt;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

/**
 * Body dùng chung cho API-SC02-01 (POST /cases/establishments — tạo mới, mọi field B phải có)
 * và API-SC02-02 (PUT /cases/{id}/establishment — cập nhật một phần, field null = không đổi).
 * 15 field Bước 1 SC-02 (FSD mục 3.2) — ánh xạ trực tiếp, JSON camelCase theo đúng quy ước hiện
 * có của repo (api-conventions.md, đối chiếu các request khác trong qldv-api).
 *
 * organizationTypeId (field 8) bị BỎ QUA khi service nhận allowedOrganizationTypeId khác null
 * (BR-SC07-01, dùng lại nguyên vẹn cho SC-07/Sprint 5) — client không thể override trong
 * trường hợp đó dù có gửi field này lên.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentCaseRequest {
    Integer brcd;
    Integer staffCount;
    LeadershipInfoRequest leadershipInfo;
    String boardDecisionNo;
    LocalDate boardDecisionDate;
    String boardDecisionSummary;
    String proposedOrganizationName;
    String organizationTypeId;
    Integer memberCount;
    Integer committeeMemberCount;
    String committeeStructure;
    List<ProposedCommitteeMemberRequest> proposedCommitteeMembers;
    String politicalStandardConclusionNo;
    LocalDate politicalStandardConclusionDate;
    List<String> attachmentIds;
}
