package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.TransformationHistoryClient;
import com.agribank.qldv_api.gateway.TransformationHistoryDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.organization_transform.OrganizationTransformRequest;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.updown.TransformationHistory;
import com.agribank.qldvutils.entity.form02.updown.TransformationHistoryDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.TransformationHistoryRpRequest;
import com.agribank.qldvutils.request.form02.UpdownOrganizationFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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
    private final TransformationHistoryDraftClient transformationHistoryDraftClient;
    private final TransformationHistoryClient transformationHistoryClient;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(TransformationHistoryDraft.FIELD_MAP);

        return combinedFieldMap;
    }

    public TransformationHistoryDraft createTransformRequest(OrganizationTransformRequest request) {
        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        if (
                Objects.equals(request.getType(), EReport01Type.UPGRADE.getId())
                        && organization.getForm().compareToIgnoreCase(request.getNewForm()) == -1
        ) {
            throw new CommonException("Yêu cầu nâng cấp không hợp lệ");
        } else if (
                Objects.equals(request.getType(), EReport01Type.DOWNGRADE.getId())
                        && organization.getForm().compareToIgnoreCase(request.getNewForm()) == 1
        ) {
            throw new CommonException("Yêu cầu hạ cấp không hợp lệ");
        }

        validateRequest(request, organization);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransformationHistoryDraft draft = new TransformationHistoryDraft();

        draft.setOrganizationCode(request.getOrganizationCode());
        draft.setType(request.getType());
        draft.setOldName(request.getOldName());
        draft.setOldForm(request.getOldForm());
        draft.setNewName(Objects.nonNull(request.getNewName()) ? request.getNewName() : organization.getName());
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

    public Request update(OrganizationTransformRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không được để trống trường id");
        }

        TransformationHistory history = historyClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND));

        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        validateRequest(request, organization);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransformationHistoryDraft draft = modelMapper.map(request, TransformationHistoryDraft.class);
        draft.setId(null);
        draft.setCreatedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        draft.setHistoryId(history.getId());
        draft = historyDraftClient.save(draft).getData();

        EForm form = Objects.equals(request.getType(), EReport01Type.UPGRADE.getId()) ? upForm : downForm;

        Request transformRequest = requestService.initializeRequest(draft, history, form, getCombinedFieldMap());
        transformRequest.setOrganizationCode(request.getOrganizationCode());
        transformRequest.setReferenceId(draft.getId());
        transformRequest.setCreatedBy(userDetails.getId());
        requestClient.save(transformRequest);

        return transformRequest;

    }

    private void validateRequest(OrganizationTransformRequest request, Organization organization) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        List<TransformationHistoryDraft> draftList = historyDraftClient.findPendingDraftByCode(request.getOrganizationCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu nâng/hạ cấp cho tổ chức đảng này");
        }
    }

    public PageResponse<TransformationHistory> getList(UpdownOrganizationFilterRequest request) {
        if (Objects.isNull(request.getOrganizationCode())) {
            request.setOrganizationCode(CommonUtils.getOrganizationByRequestedUser());
        }

        Page<TransformationHistory> historyPage = historyClient.getList(request).getData();

        PageResponse<TransformationHistory> response = new PageResponse<>();
        response.setData(historyPage.getContent());
        response.setCurrentPage(historyPage.getNumber());
        response.setTotalPages(historyPage.getTotalPages());
        response.setTotalItems(historyPage.getTotalElements());

        return response;
    }

    public TransformationHistory getDetail(String id) {
        return historyClient.findById(id).getData().orElse(null);
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransformationHistoryDraft draft = historyDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        TransformationHistory history = modelMapper.map(draft, TransformationHistory.class);

        return approveDraftRequest(draft, history);
    }

    @Override
    public boolean applyUpdate(String draftId) {
        TransformationHistoryDraft draft = historyDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        TransformationHistory history = historyClient.findById(draft.getHistoryId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        history = modelMapper.map(draft, TransformationHistory.class);
        history.setId(draft.getHistoryId());

        return approveDraftRequest(draft, history);
    }

    private boolean approveDraftRequest(TransformationHistoryDraft draft, TransformationHistory history) {
        Organization organization = organizationClient.findByCode(history.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) return false;

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        organization.setName(draft.getNewName());
        organization.setForm(draft.getNewForm());

        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draft.setApprovedBy(userDetails.getId());

        organizationClient.save(organization);
        historyClient.save(history);
        historyDraftClient.save(draft);

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

    public List<TransformationHistory> findByOrganizationCodeAndDate(TransformationHistoryRpRequest request){
        return transformationHistoryClient.findByOrganizationCodeAndDate(request).getData();
    }
}
