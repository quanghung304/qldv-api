package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.EstablishmentDissolveClient;
import com.agribank.qldv_api.gateway.EstablishmentDissolveDraftClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.establishment_dissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.ApproveDissolveRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EstablishmentDissolveDraftService implements EntityHandler {
    private final ModelMapper modelMapper;
    private final EstablishmentDissolveDraftClient client;
    private final EstablishmentDissolveClient dissolveClient;
    private final RequestClient requestClient;
    private final OrganizationClient organizationClient;
    private final OrganizationHistoryClient historyClient;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final CheckAuthorityService checkAuthorityService;
    private final EstablishmentDissolveService establishmentDissolveService;
    private final RequestService requestService;

    private final EForm form = EForm.BIEU_02_ESTA;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(EstablishmentDissolveDraft.FIELD_MAP);

        return combinedFieldMap;
    }

    public EstablishmentDissolveDraft createOrUpdate(EstablishmentDissolveRequest request) {
        List<EstablishmentDissolveDraft> draftList = client.findPendingDraftByCode(request.getOrganizationCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu với tổ chức đảng này");
        }

        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.equals(request.getType(), EReport01Type.DISSOLVE.getId()) && Objects.isNull(organization)) {
            throw new CommonException("Tổ chức đảng không tồn tại");
        }

        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        EstablishmentDissolveDraft establishmentDissolve = null;
        if (Objects.nonNull(request.getId())) {
            establishmentDissolve = client.findById(request.getId()).getData();
        }

        if (Objects.isNull(establishmentDissolve)){
            establishmentDissolve = new EstablishmentDissolveDraft();
        }

        UserDetailsImpl userRequested = userService.getUserRequested();

        establishmentDissolve.setOrganizationCode(request.getOrganizationCode());
        establishmentDissolve.setName(organization.getName());
        establishmentDissolve.setForm(organization.getForm());
        establishmentDissolve.setType(request.getType());
        establishmentDissolve.setConclusionNumber(request.getConclusionNumber());
        establishmentDissolve.setConclusionDate(request.getConclusionDate());
        establishmentDissolve.setDecisionNumber(request.getDecisionNumber());
        establishmentDissolve.setDecisionDate(request.getDecisionDate());
        establishmentDissolve.setEffectiveDate(request.getEffectiveDate());
        establishmentDissolve.setStatus(EApprovalStatus.PENDING.getId());
        establishmentDissolve.setCreatedBy(userRequested.getId());

        establishmentDissolve = client.save(establishmentDissolve).getData();

        Request transformRequest = requestService.initializeRequest(establishmentDissolve, null, form, getCombinedFieldMap());
        transformRequest.setOrganizationCode(establishmentDissolve.getOrganizationCode());
        transformRequest.setReferenceId(establishmentDissolve.getId());
        transformRequest.setCreatedBy(userRequested.getId());
        requestClient.save(transformRequest);

        return establishmentDissolve;
    }

    public EstablishmentDissolveDraft update(EstablishmentDissolveRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        EstablishmentDissolve  dissolve =  dissolveClient.findById(request.getId()).getData().orElse(null);

        if (Objects.isNull(dissolve)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        Organization organization = organizationService.findByCode(request.getOrganizationCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Không tồn tại tổ chức Đảng này! vui lòng kiểm tra lại");
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        EstablishmentDissolveDraft dissolveDraft = EstablishmentDissolveDraft.builder()
                .organizationCode(request.getOrganizationCode())
                .name(request.getName())
                .form(request.getForm())
                .status(EApprovalStatus.PENDING.getId())
                .createdBy(userRequested.getId())
                .refId(request.getId())
                .build();
        dissolveDraft.setConclusionNumber(request.getConclusionNumber());
        dissolveDraft.setConclusionDate(request.getConclusionDate());
        dissolveDraft.setDecisionNumber(request.getDecisionNumber());
        dissolveDraft.setDecisionDate(request.getDecisionDate());
        dissolveDraft.setEffectiveDate(request.getEffectiveDate());

        dissolveDraft = client.save(dissolveDraft).getData();

        Request establishDisolveRequest = requestService.initializeRequest(dissolveDraft, dissolve, form, getCombinedFieldMap());
        establishDisolveRequest.setOrganizationCode(request.getOrganizationCode());
        establishDisolveRequest.setReferenceId(dissolveDraft.getId());
        establishDisolveRequest.setCreatedBy(userRequested.getId());
        requestClient.save(establishDisolveRequest);

        return dissolveDraft;
    }


    public String delete(String id){
        EstablishmentDissolveDraft establishmentDissolveDraft = client.findById(id).getData();
        if (Objects.isNull(establishmentDissolveDraft)) {
            throw new CommonException("Không xóa được yêu cầu! Vui lòng kiểm tra lại sau");
        }

        if (EApprovalStatus.PENDING.getId() != establishmentDissolveDraft.getStatus()){
            throw new CommonException("Yêu cầu đã được duyệt, nên bạn không thể xóa yêu cầu này!");
        }

        client.delete(id);
        return "Xóa yêu cầu thành công!";
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        EstablishmentDissolveDraft draft = client.findById(draftId).getData();

        if (Objects.isNull(draft)) return false;

        EstablishmentDissolve establishmentDissolve = modelMapper.map(draft, EstablishmentDissolve.class);
        Organization organization = organizationClient.findByCode(establishmentDissolve.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) return false;

        organization.setStatus(EOrganizationStatus.NO.getStatus());

        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draft.setApprovedBy(userDetails.getId());

        OrganizationHistory history = OrganizationHistory.builder()
                .name(organization.getName())
                .type(establishmentDissolve.getType())
                .effectiveDate(establishmentDissolve.getEffectiveDate())
                .refId(establishmentDissolve.getId())
                .build();
        history.setCode(organization.getCode());

        ApproveDissolveRequest request = ApproveDissolveRequest.builder()
                .organization(organization)
                .dissolve(establishmentDissolve)
                .draft(draft)
                .history(history)
                .build();

        dissolveClient.saveEntities(request);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        EstablishmentDissolveDraft draft = client.findById(referenceId).getData();

        if (Objects.isNull(draft)) return false;

        EstablishmentDissolve establishmentDissolve = establishmentDissolveService.findById(draft.getRefId());
        if (Objects.isNull(establishmentDissolve)) return false;

        establishmentDissolve.setName(draft.getName());
        establishmentDissolve.setForm(draft.getForm());
        establishmentDissolve.setDecisionCommittee(draft.getDecisionCommittee());
        establishmentDissolve.setConclusionNumber(draft.getConclusionNumber());
        establishmentDissolve.setConclusionDate(draft.getConclusionDate());
        establishmentDissolve.setEffectiveDate(draft.getEffectiveDate());
        establishmentDissolve.setDecisionDate(draft.getDecisionDate());
        establishmentDissolve.setDecisionNumber(draft.getDecisionNumber());

        Organization organization = organizationClient.findByCode(establishmentDissolve.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) return false;

        String organizationCode = "";
        if (!establishmentDissolve.getOrganizationCode().equals(draft.getOrganizationCode())) {
            organization.setStatus(EOrganizationStatus.YES.getStatus());
            organizationClient.save(organization);
            organizationCode = draft.getOrganizationCode();
            establishmentDissolve.setOrganizationCode(organizationCode);
        }

        if (!organizationCode.isBlank()){
            organization = organizationClient.findByCode(organizationCode).getData();
        }

        if (Objects.isNull(organization)) return false;
        organization.setStatus(EOrganizationStatus.NO.getStatus());

        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draft.setApprovedBy(userService.getUserRequested().getId());

        OrganizationHistory history = historyClient.findByRefId(EReport01Type.DISSOLVE.getId(), establishmentDissolve.getId()).getData();

        if (Objects.nonNull(history)) {
            history.setCode(organization.getCode());
            history.setName(organization.getName());
            history.setEffectiveDate(establishmentDissolve.getEffectiveDate());
        } else {
            history = OrganizationHistory.builder()
                    .name(organization.getName())
                    .type(establishmentDissolve.getType())
                    .effectiveDate(establishmentDissolve.getEffectiveDate())
                    .refId(establishmentDissolve.getId())
                    .build();
            history.setCode(organization.getCode());
        }
        ApproveDissolveRequest request = ApproveDissolveRequest.builder()
                .organization(organization)
                .dissolve(establishmentDissolve)
                .draft(draft)
                .history(history)
                .build();

        dissolveClient.saveEntities(request);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String draftId) {
        EstablishmentDissolveDraft draft = client.findById(draftId).getData();

        if (Objects.isNull(draft)) return;
        draft.setStatus(EApprovalStatus.DENIED.getId());
    }
}
