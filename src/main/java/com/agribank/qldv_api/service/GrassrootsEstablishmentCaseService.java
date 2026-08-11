package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.request.casemgmt.EstablishmentCaseRequest;
import com.agribank.qldv_api.request.casemgmt.GrassrootsEstablishmentCaseRequest;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseResponse;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.enums.EAuthorityLevel;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * API-SC07 (POST /cases/grassroots-establishments) — task LẮP RÁP, KHÔNG viết lại logic: adapt
 * {@link GrassrootsEstablishmentCaseRequest} (subset field, KHÔNG có organizationTypeId/
 * leadershipInfo/committeeStructure) thành {@link EstablishmentCaseRequest} rồi gọi lại NGUYÊN
 * VẸN {@code EstablishmentCaseService.createEstablishmentCase} — đúng RR-03 (service dùng chung
 * nhận authorityLevel/originFlow/allowedOrganizationTypeId làm tham số tường minh do CALLER
 * quyết định, xem javadoc {@code EstablishmentCaseService}). KHÔNG có service update riêng —
 * PUT /cases/{id}/establishment (đã có, S2-04) dùng được nguyên vẹn cho hồ sơ grassroots.
 */
@Service
@RequiredArgsConstructor
public class GrassrootsEstablishmentCaseService {
    /** Mã loại hình "Chi bộ trực thuộc đảng bộ cơ sở" trong PMDV_ORGANIZATION_TYPE — tra theo code, KHÔNG hardcode UUID (xem prompt_S5-01). */
    private static final String CBTT_DUCS_ORGANIZATION_TYPE_CODE = "CBTT_DUCS";

    private final EstablishmentCaseService establishmentCaseService;
    private final OrganizationTypeClient organizationTypeClient;

    public EstablishmentCaseResponse createGrassrootsEstablishmentCase(GrassrootsEstablishmentCaseRequest request) {
        String cbttDucsTypeId = requireCbttDucsOrganizationTypeId();
        EstablishmentCaseRequest adapted = toEstablishmentCaseRequest(request);
        return establishmentCaseService.createEstablishmentCase(
                adapted, EAuthorityLevel.GRASSROOTS_LEVEL.getId(), request.getFlowType(), cbttDucsTypeId);
    }

    private String requireCbttDucsOrganizationTypeId() {
        OrganizationType organizationType = organizationTypeClient.findByCode(CBTT_DUCS_ORGANIZATION_TYPE_CODE)
                .getData()
                .orElseThrow(() -> new CommonException("Danh mục loại hình tổ chức đảng 'Chi bộ trực thuộc đảng bộ cơ sở' "
                        + "(code=" + CBTT_DUCS_ORGANIZATION_TYPE_CODE + ") chưa được seed"));
        return organizationType.getId();
    }

    /**
     * organizationTypeId/leadershipInfo/committeeStructure để null có chủ đích — service dùng
     * chung sẽ ghi đè organizationTypeId bằng allowedOrganizationTypeId (CBTT_DUCS), còn 2 field
     * kia SC-07 không thu thập nên cứ để null (setter null-safe ở applyUpdates()/service, không
     * bắt buộc ở tầng DB — xem CaseEstablishment entity).
     */
    private EstablishmentCaseRequest toEstablishmentCaseRequest(GrassrootsEstablishmentCaseRequest request) {
        EstablishmentCaseRequest adapted = new EstablishmentCaseRequest();
        adapted.setBrcd(request.getBrcd());
        adapted.setStaffCount(request.getStaffCount());
        adapted.setProposedOrganizationName(request.getProposedOrganizationName());
        adapted.setMemberCount(request.getMemberCount());
        adapted.setCommitteeMemberCount(request.getCommitteeMemberCount());
        adapted.setBoardDecisionNo(request.getBoardDecisionNo());
        adapted.setBoardDecisionDate(request.getBoardDecisionDate());
        adapted.setBoardDecisionSummary(request.getBoardDecisionSummary());
        adapted.setProposedCommitteeMembers(request.getProposedCommitteeMembers());
        adapted.setPoliticalStandardConclusionNo(request.getPoliticalStandardConclusionNo());
        adapted.setPoliticalStandardConclusionDate(request.getPoliticalStandardConclusionDate());
        return adapted;
    }
}
