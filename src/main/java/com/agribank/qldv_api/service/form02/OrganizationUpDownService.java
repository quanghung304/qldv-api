package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.gateway.form02.updown.OrganizationUpDownClient;
import com.agribank.qldv_api.gateway.form02.updown.OrganizationUpDownDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.OrganizationUpDownRequest;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDown;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDownDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.AprroveUpDownRequest;
import com.agribank.qldvutils.request.form02.OrganizationUpDownRpRequest;
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
public class OrganizationUpDownService implements EntityHandler {
    private final OrganizationUpDownDraftClient updownDraftClient;
    private final OrganizationClient organizationClient;
    private final OrganizationUpDownClient updownClient;
    private final RequestClient requestClient;
    private final OrganizationHistoryClient historyClient;
    private final CheckAuthorityService checkAuthorityService;
    private final OrganizationService organizationService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;

    private final EForm upForm = EForm.BIEU_02_UP;
    private final EForm downForm = EForm.BIEU_02_DOWN;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationUpDownDraft.FIELD_MAP);

        return combinedFieldMap;
    }

    public OrganizationUpDownDraft createTransformRequest(OrganizationUpDownRequest request) {
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

        OrganizationUpDownDraft draft = new OrganizationUpDownDraft();

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
        OrganizationUpDownDraft OrganizationUpDownDraft = updownDraftClient.save(draft).getData();

        EForm form = Objects.equals(request.getType(), EReport01Type.UPGRADE.getId()) ? upForm : downForm;

        Request transformRequest = requestService.initializeRequest(OrganizationUpDownDraft, null, form, getCombinedFieldMap());
        transformRequest.setOrganizationCode(request.getOrganizationCode());
        transformRequest.setReferenceId(OrganizationUpDownDraft.getId());
        transformRequest.setCreatedBy(userDetails.getId());
        requestClient.save(transformRequest);

        return OrganizationUpDownDraft;
    }

    public List<OrganizationUpDownDraft> getDrafttList(Integer status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return updownDraftClient.getList(userDetails.getOrganizationCode(), status).getData();
    }

    public OrganizationUpDownDraft getDraft(String id) {
        return updownDraftClient.findById(id).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));
    }

    public OrganizationUpDownDraft updateDraft(OrganizationUpDownRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không được để trống trường id");
        }

        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        Request requestDetail = requestClient.findByReferenceId(request.getId()).getData();

        if (Objects.isNull(requestDetail)) {
            throw new CommonException("Yêu cầu phê duyệt không tồn tại!");
        }

        if (requestDetail.getStatus() != EApprovalStatus.PENDING.getId()) {
            throw new CommonException("Yêu cầu đã được phê duyệt hoặc bị từ chối!");
        }

        OrganizationUpDownDraft draft = updownDraftClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));
        draft.setOrganizationCode(request.getOrganizationCode());
        draft.setOldForm(request.getOldForm());
        draft.setOldName(request.getOldName());
        draft.setNewForm(request.getNewForm());
        draft.setNewName(request.getNewName());
        draft.setType(request.getType());
        draft.setConclusionDate(request.getConclusionDate());
        draft.setConclusionNumber(request.getConclusionNumber());
        draft.setDecisionCommittee(request.getDecisionCommittee());
        draft.setDecisionNumber(request.getDecisionNumber());
        draft.setDecisionDate(request.getDecisionDate());
        draft.setEffectiveDate(request.getEffectiveDate());

        requestDetail.setNewData(requestService.createJsonData(draft, getCombinedFieldMap()));
        requestClient.save(requestDetail);

        return updownDraftClient.save(draft).getData();
    }

    public Request update(OrganizationUpDownRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không được để trống trường id");
        }

        OrganizationUpDown history = updownClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND));

        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)){
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        validateRequest(request, organization);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationUpDownDraft draft = modelMapper.map(request, OrganizationUpDownDraft.class);
        draft.setId(null);
        draft.setCreatedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        draft.setHistoryId(history.getId());
        draft = updownDraftClient.save(draft).getData();

        EForm form = Objects.equals(request.getType(), EReport01Type.UPGRADE.getId()) ? upForm : downForm;

        Request transformRequest = requestService.initializeRequest(draft, history, form, getCombinedFieldMap());
        transformRequest.setOrganizationCode(request.getOrganizationCode());
        transformRequest.setReferenceId(draft.getId());
        transformRequest.setCreatedBy(userDetails.getId());
        requestClient.save(transformRequest);

        return transformRequest;

    }

    private void validateRequest(OrganizationUpDownRequest request, Organization organization) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        List<OrganizationUpDownDraft> draftList = updownDraftClient.findPendingDraftByCode(request.getOrganizationCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu nâng/hạ cấp cho tổ chức đảng này");
        }
    }

    public PageResponse<OrganizationUpDown> getList(UpdownOrganizationFilterRequest request) {
        if (Objects.isNull(request.getOrganizationCode())) {
            request.setOrganizationCode(CommonUtils.getOrganizationByRequestedUser());
        }

        Page<OrganizationUpDown> historyPage = updownClient.getList(request).getData();

        PageResponse<OrganizationUpDown> response = new PageResponse<>();
        response.setData(historyPage.getContent());
        response.setCurrentPage(historyPage.getNumber());
        response.setTotalPages(historyPage.getTotalPages());
        response.setTotalItems(historyPage.getTotalElements());

        return response;
    }

    public OrganizationUpDown getDetail(String id) {
        return updownClient.findById(id).getData().orElse(null);
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        OrganizationUpDownDraft draft = updownDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        OrganizationUpDown upDown = modelMapper.map(draft, OrganizationUpDown.class);

        return approveDraftRequest(draft, upDown);
    }

    @Override
    public boolean applyUpdate(String draftId) {
        OrganizationUpDownDraft draft = updownDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        OrganizationUpDown updown = updownClient.findById(draft.getHistoryId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));

        updown = modelMapper.map(draft, OrganizationUpDown.class);
        updown.setId(draft.getHistoryId());

        return approveDraftRequest(draft, updown);
    }

    private boolean approveDraftRequest(OrganizationUpDownDraft draft, OrganizationUpDown updown) {
        Organization organization = organizationClient.findByCode(updown.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) return false;

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        organization.setName(draft.getNewName());
        organization.setForm(draft.getNewForm());

        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draft.setApprovedBy(userDetails.getId());

        OrganizationHistory history = null;

        if (Objects.nonNull(updown.getId())) {
            history = historyClient.findByRefId(updown.getType(), updown.getId()).getData();
        }

        if (Objects.isNull(history)) {
            history = OrganizationHistory.builder()
                    .code(organization.getCode())
                    .name(organization.getName())
                    .type(draft.getType())
                    .effectiveDate(updown.getEffectiveDate())
                    .build();
        } else {
            history.setCode(organization.getCode());
            history.setName(organization.getName());
            history.setEffectiveDate(updown.getEffectiveDate());
        }

        AprroveUpDownRequest request = AprroveUpDownRequest.builder()
                .organization(organization)
                .upDown(updown)
                .draft(draft)
                .history(history)
                .build();

        updownClient.saveEntities(request);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return true;
    }

    @Override
    public void setDenied(String draftId) {
        OrganizationUpDownDraft draft = updownDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu nâng/hạ cấp."));
        draft.setStatus(EApprovalStatus.DENIED.getId());
    }

    public List<OrganizationUpDown> findByOrganizationCodeAndDate(OrganizationUpDownRpRequest request){
        return updownClient.findByOrganizationCodeAndDate(request).getData();
    }
}
