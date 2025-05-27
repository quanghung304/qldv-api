package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.TransformationHistoryClient;
import com.agribank.qldv_api.gateway.TransformationHistoryDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.request.organizationTransform.OrganizationTransformRequest;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TransformationHistoryService implements EntityHandler {
    private final TransformationHistoryDraftClient historyDraftClient;
    private final OrganizationClient organizationClient;
    private final TransformationHistoryClient historyClient;
    private final RequestClient requestClient;
    private final CheckAuthorityService checkAuthorityService;
    private final OrganizationService organizationService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;

    private final EForm upForm = EForm.BIEU_02_UP;
    private final EForm downForm = EForm.BIEU_02_DOWN;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new HashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(TransformationHistoryDraft.FIELD_MAP);

        return combinedFieldMap;
    }

    public TransformationHistoryDraft createTransformRequest(OrganizationTransformRequest request) {
        Organization organization = organizationService.findByCode(request.getOrganizationCode());
        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        List<TransformationHistoryDraft> draftList = historyDraftClient.findPendingDraftByCode(request.getOrganizationCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu nâng/hạ cấp cho tổ chức đảng này");
        }

        TransformationHistoryDraft draft = new TransformationHistoryDraft();

        draft.setOrganizationCode(request.getOrganizationCode());
        draft.setType(request.getType());
        draft.setOldName(request.getOldName());
        draft.setOldForm(request.getOldForm());
        draft.setNewName(request.getNewName());
        draft.setNewForm(request.getNewForm());
        draft.setDecisionCommittee(request.getDecisionCommittee());
        draft.setConclusionNumber(request.getConclusionNumber());
        draft.setConclusionDate(request.getConclusionDate());
        draft.setDecisionNumber(request.getDecisionNumber());
        draft.setDecisionDate(request.getDecisionDate());
        draft.setEffectiveDate(request.getEffectiveDate());
        draft.setCreatedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        TransformationHistoryDraft transformationHistoryDraft = historyDraftClient.save(draft).getData();

        EForm form = Objects.equals(request.getType(), EReport01Type.UPGRADE.getId()) ? upForm : downForm;

        Request transformRequest = requestService.initializeRequest(transformationHistoryDraft, null, form, getCombinedFieldMap());
        transformRequest.setOrganizationCode(request.getOrganizationCode());
        transformRequest.setReferenceId(transformationHistoryDraft.getId());
        transformRequest.setCreatedBy(userDetails.getId());
        transformRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(transformRequest);

        return transformationHistoryDraft;
    }

    public List<TransformationHistoryDraft> getDrafttList(Integer status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return historyDraftClient.getList(userDetails.getOrganizationCode(), status).getData();
    }

    public TransformationHistoryDraft getDraft(String id) {
        return historyDraftClient.findById(id).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));
    }

    public List<String> update(List<ApproveRequest> requestList) {
        try {
            List<String> idList = new ArrayList<>();
            Map<String, Integer> idAndStatusMap = new HashMap<>();

            for (ApproveRequest request : requestList) {
                idList.add(request.getId());
                idAndStatusMap.put(request.getId(), request.getStatus());
            }

            List<TransformationHistoryDraft> draftList = historyDraftClient.findAllById(idList).getData();

            List<String> codeList = new ArrayList<>();
            for (TransformationHistoryDraft draft : draftList) {
                codeList.add(draft.getOrganizationCode());
            }

            List<Organization> organizationList = organizationClient.findAllByCode(codeList).getData();
            Map<String, Organization> codeAndOrganizationMap = new HashMap<>();
            for (Organization organization: organizationList) {
                codeAndOrganizationMap.put(organization.getCode(), organization);
            }

            List<String> response = new ArrayList<>();
            List<TransformationHistoryDraft> updatedDrafts = new ArrayList<>();
            List<Organization> updatedOrganizations = new ArrayList<>();
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            for (TransformationHistoryDraft draft : draftList) {
                if (
                        Objects.equals(userDetails.getOrganizationCode(), Constants.BTCDU_CODE) || draft.getOrganizationCode().contains(userDetails.getOrganizationCode())
                ) {
                    draft.setStatus(idAndStatusMap.get(draft.getId()));
                    draft.setApprovedBy(userDetails.getStaffCode());
                    updatedDrafts.add(draft);

                    Organization organization = codeAndOrganizationMap.get(draft.getOrganizationCode());
                    organization.setName(draft.getNewName());
                    organization.setForm(draft.getNewForm());
                    updatedOrganizations.add(organization);

                    response.add("Phe duyet thanh cong yeu cau ma to chuc dang " + draft.getOrganizationCode());
                    continue;
                }

                response.add("Phe duyet that bai yeu cau ma to chuc dang " + draft.getOrganizationCode());
            }

            organizationClient.saveAll(updatedOrganizations);
            historyDraftClient.saveAll(updatedDrafts);

            return response;
        } catch (Exception e) {
            throw new CommonException("Phe duyet that bai");
        }
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransformationHistoryDraft draft = historyDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        if (Objects.equals(userDetails.getOrganizationCode(), Constants.BTCDU_CODE) || draft.getOrganizationCode().contains(userDetails.getOrganizationCode())) {
            TransformationHistory history = modelMapper.map(draft, TransformationHistory.class);

            Organization organization = organizationClient.findByCode(history.getOrganizationCode()).getData();
            if (Objects.isNull(organization)) return false;

            organization.setName(draft.getNewName());
            organization.setForm(draft.getNewForm());

            draft.setStatus(EApprovalStatus.APPROVED.getId());
            draft.setApprovedBy(userDetails.getId());

            organizationClient.save(organization);
            historyClient.save(history);
            historyDraftClient.save(draft);
        }

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return true;
    }

    @Override
    public void setDenied(String draftId) {
        TransformationHistoryDraft draft = historyDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));
        draft.setStatus(EApprovalStatus.DENIED.getId());
    }
}
