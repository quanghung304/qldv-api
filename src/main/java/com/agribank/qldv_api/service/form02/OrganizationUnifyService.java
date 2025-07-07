package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.UnifyOrgUpdateRequest;
import com.agribank.qldv_api.request.form02.UnifyOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.DvOrgService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrganizationUnifyService extends MergeUnifyService implements EntityHandler {
    static final EForm form = EForm.BIEU_02_UNION;

    public OrganizationUnifyService(
            ObjectMapper objectMapper,
            ModelMapper modelMapper,
            DVClient dvClient,
            OrganizationClient organizationClient,
            OrganizationMergeClient mergeClient,
            OrganizationMergeDetailClient mergeDetailClient,
            OrganizationMergeDraftClient mergeDraftClient,
            OrganizationMergeDetailDraftClient mergeDetailDraftClient,
            RequestClient requestClient,
            DvOrgHistoryClient dvOrgHistoryClient,
            RequestService requestService,
            OrganizationService organizationService,
            DvOrgService dvOrgService,
            CheckAuthorityService checkAuthorityService
    ) {
        super(
                objectMapper, modelMapper, dvClient, organizationClient, mergeClient, mergeDetailClient, mergeDraftClient,
                mergeDetailDraftClient, requestClient, dvOrgHistoryClient, requestService, organizationService, dvOrgService,
                checkAuthorityService
        );
    }

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationMergeDraft.FIELD_MAP_MERGE);

        return combinedFieldMap;
    }

    public Request createUnifyRequest(UnifyOrganizationRequest request) {
        List<Organization> unifiedOrganizations = getUnifiedOrganizations(request.getUnifyCodes(), false);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft organizationUnify = modelMapper.map(request, OrganizationMergeDraft.class);
        organizationUnify.setType(EReport01Type.UNION.getId());
        organizationUnify.setCreatedBy(userDetails.getStaffCode());
        organizationUnify = mergeDraftClient.save(organizationUnify).getData();

        List<OrganizationMergeDetailDraft> unifyDetails = new ArrayList<>();
        for (Organization organization: unifiedOrganizations) {
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(organizationUnify.getId())
                    .oldCode(organization.getCode())
                    .oldName(organization.getName())
                    .build();

            unifyDetails.add(detail);
        }
        mergeDetailDraftClient.saveAll(unifyDetails);

        Request unifyRequest = requestService.initializeRequest(organizationUnify, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(organizationUnify, unifyDetails, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        unifyRequest.setNewData(jsonData);
        unifyRequest.setReferenceId(organizationUnify.getId());
        unifyRequest.setCreatedBy(userDetails.getId());
        unifyRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(unifyRequest);

        return unifyRequest;
    }

    private List<Organization> getUnifiedOrganizations(List<String> unifyCodes, boolean isUpdate) {
        List<Organization> unifiedOrganizations = organizationClient.findAllByCode(unifyCodes).getData();
        if (!isUpdate) {
            unifiedOrganizations = unifiedOrganizations.stream()
                    .filter(x -> Objects.equals(x.getStatus(), EOrganizationStatus.YES.getStatus()))
                    .toList();
        }

        if (unifiedOrganizations.isEmpty()) {
            throw new CommonException("Danh sách chi bộ nhận hợp nhất không hợp lệ");
        }

        return unifiedOrganizations;
    }

    @Override
    public PageResponse<OrganizationMerResponse> getList(SearchOrganizationUnionRequest request) {
        request.setType(EReport01Type.UNION.getId());
        return search(request);
    }

    public RequestResponse update(UnifyOrgUpdateRequest request){
        OrganizationMerge oldUnify = mergeClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu. Vui lòng kiểm tra lại"));

        List<Organization> unifiedOrganizations = getUnifiedOrganizations(request.getUnifyCodes(), true);

        UserDetailsImpl userRequested = getUserRequested();

        OrganizationMergeDraft organizationUnify = OrganizationMergeDraft.builder()
                .organizationCode(request.getOrganizationCode())
                .organizationName(request.getOrganizationName())
                .form(request.getForm())
                .type(EReport01Type.UNION.getId())
                .refId(oldUnify.getId())
                .build();
        organizationUnify.setCreatedBy(userRequested.getStaffCode());
        organizationUnify.setEffectiveDate(request.getEffectiveDate());
        organizationUnify.setDecisionCommittee(request.getDecisionCommittee());
        organizationUnify.setDecisionDate(request.getDecisionDate());
        organizationUnify.setDecisionNumber(request.getDecisionNumber());
        organizationUnify.setConclusionDate(request.getConclusionDate());
        organizationUnify.setConclusionNumber(request.getConclusionNumber());
        organizationUnify = mergeDraftClient.save(organizationUnify).getData();

        List<OrganizationMergeDetailDraft> unifyDetails = new ArrayList<>();
        for (Organization organization: unifiedOrganizations) {
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(organizationUnify.getId())
                    .oldCode(organization.getCode())
                    .oldName(organization.getName())
                    .build();

            unifyDetails.add(detail);
        }
        mergeDetailDraftClient.saveAll(unifyDetails);
        Request unifyRequest = requestService.initializeRequest(organizationUnify, oldUnify, form, getCombinedFieldMap());

        List<OrganizationMergeDetail> oldDetails = mergeDetailClient.findByRefId(oldUnify.getId()).getData();

        String newJsonData = createJsonData(organizationUnify, unifyDetails, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        String oldJsonData = createJsonData(oldUnify, oldDetails, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        unifyRequest.setNewData(newJsonData);
        unifyRequest.setOldData(oldJsonData);

        unifyRequest.setReferenceId(organizationUnify.getId());
        unifyRequest.setCreatedBy(userRequested.getId());
        unifyRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(unifyRequest);

        return modelMapper.map(unifyRequest, RequestResponse.class);
    }

    @Override
    @Transactional
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        OrganizationMergeDraft draft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft) || !Objects.equals(draft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Yêu cầu không hợp lệ");
        }

        List<OrganizationMergeDetailDraft> detailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        if (detailDrafts.isEmpty()) {
            return false;
        }
        //Luu ho so ban goc
        OrganizationMerge organizationUnify = modelMapper.map(draft, OrganizationMerge.class);
        organizationUnify = mergeClient.save(organizationUnify).getData();

        //luu danh sach chi nhanh dc sap nhap/hop nhat
        List<OrganizationMergeDetail> details = new ArrayList<>();
        List<String> unifiedCodes = new ArrayList<>();

        for (OrganizationMergeDetailDraft detailDraft: detailDrafts) {
            OrganizationMergeDetail detail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            detail.setReferenceId(organizationUnify.getId());
            details.add(detail);
            unifiedCodes.add(detailDraft.getOldCode());
        }
        mergeDetailClient.saveAll(details);

        Organization organization = new Organization();
        String newOrganizationCode = organizationUnify.getOrganizationCode();
        organization.setCode(newOrganizationCode);
        organization.setName(organizationUnify.getOrganizationName());
        organization.setForm(organizationUnify.getForm());
        if (newOrganizationCode.length() == Constants.FORM_B_NAME_LENGTH) {
            organization.setParentCode(Constants.DANG_UY_AGRIBANK_CODE);
        }
        else{
            organization.setParentCode(newOrganizationCode.substring(0, newOrganizationCode.length() - 2));
        }

        organizationClient.save(organization);

        //chuyen cac dang vien tu TCD bi sap nhap/hop nhat sang TCD nhan sap nhap/hop nhat
        unifyOrganization(unifiedCodes, organizationUnify.getOrganizationCode());

        //luu trang thai draft
        draft.setApprovedBy(userDetails.getStaffCode());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        mergeDraftClient.save(draft);
        return true;
    }

    private void unifyOrganization(List<String> unifiedCodes, String unifyingCode) {
        List<Organization> unifiedOrganizations = organizationClient.findAllByCode(unifiedCodes).getData();

        for(Organization organization: unifiedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DV> unifiedMembers = dvClient.findByOrganizationCodeActiveIn(unifiedCodes).getData();
        List<DvOrgHistory> dvOrgHistories = new ArrayList<>();
        List<String> staffCodes = new ArrayList<>();
        for (DV member: unifiedMembers) {
            DvOrgHistory dvOrgHistory = DvOrgHistory.builder()
                    .oldOrgCode(member.getOrganizationCode())
                    .newOrgCode(unifyingCode)
                    .staffCode(member.getStaffCode())
                    .build();

            dvOrgHistories.add(dvOrgHistory);
            staffCodes.add(member.getStaffCode());
            member.setOrganizationCode(unifyingCode);
        }

        List<DvOrgHistory> dvOrgHistoryListInDb = dvOrgService.findByStaffCodes(staffCodes);
        if (!dvOrgHistoryListInDb.isEmpty()) {
            Map<String, DvOrgHistory> dvOrgHistoriesMap = new HashMap<>();
            for (DvOrgHistory dvOrgHistory: dvOrgHistoryListInDb) {
                dvOrgHistoriesMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }

            for (DvOrgHistory dvOrgHistory: dvOrgHistories) {
                DvOrgHistory dvOrgHis = dvOrgHistoriesMap.getOrDefault(dvOrgHistory.getStaffCode(), null);
                if (Objects.nonNull(dvOrgHis)) {
                    dvOrgHistory.setId(dvOrgHis.getId());
                }
            }
        }

        organizationClient.saveAll(unifiedOrganizations);
        dvClient.saveAll(unifiedMembers);

        if (!dvOrgHistories.isEmpty()) {
            dvOrgService.saveAll(dvOrgHistories);
        }
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        OrganizationMergeDraft organizationMergeDraft = mergeDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationMergeDetailDraft> detailDrafts = mergeDetailDraftClient.findByRefId(organizationMergeDraft.getId()).getData();
        if (detailDrafts.isEmpty()) {
            return false;
        }

        OrganizationMerge organizationMerge = mergeClient.findById(organizationMergeDraft.getRefId())
                .getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationMergeDetail> organizationMergeDetails = mergeDetailClient.findByRefId(organizationMerge.getId()).getData();
        if (organizationMergeDetails.isEmpty()) {
            return false;
        }

        organizationMerge.setForm(organizationMergeDraft.getForm());
        organizationMerge.setOrganizationCode(organizationMergeDraft.getOrganizationCode());
        organizationMerge.setOrganizationName(organizationMergeDraft.getOrganizationName());
        organizationMerge.setEffectiveDate(organizationMergeDraft.getEffectiveDate());
        organizationMerge.setDecisionCommittee(organizationMergeDraft.getDecisionCommittee());
        organizationMerge.setDecisionDate(organizationMergeDraft.getDecisionDate());
        organizationMerge.setDecisionNumber(organizationMergeDraft.getDecisionNumber());
        organizationMerge.setConclusionDate(organizationMergeDraft.getConclusionDate());
        organizationMerge.setConclusionNumber(organizationMergeDraft.getConclusionNumber());

        Map<String, OrganizationMergeDetailDraft> organizationMergeDetailDraftMap = new HashMap<>();
        for (OrganizationMergeDetailDraft detailDraft: detailDrafts) {
            organizationMergeDetailDraftMap.put(detailDraft.getOldCode(), detailDraft);
        }
        //biến lấy danh sách để xóa bảng organizationMergeDetail đi
        List<OrganizationMergeDetail> removeList = new ArrayList<>();
        //Lấy danh sách organizationMergeDetail mới
        List<OrganizationMergeDetail> newDetailList = new ArrayList<>();
        //Lấy mã TCD bị nhầm nhằm khôi phục lại TCD đã set trạng thái ngừng hoặt động
        //và set dv khôi phục lại tCD cũ
        List<String> orgCodeSetActive = new ArrayList<>();

        Map<String, OrganizationMergeDetail> organizationMergeDetailMap = new HashMap<>();
        for (OrganizationMergeDetail detail: organizationMergeDetails) {
            OrganizationMergeDetailDraft detailDraft = organizationMergeDetailDraftMap.getOrDefault(detail.getOldCode(), null);
            if (Objects.isNull(detailDraft)) {
                removeList.add(detail);
                orgCodeSetActive.add(detail.getOldCode());
            }else {
                newDetailList.add(detail);
                organizationMergeDetailMap.put(detail.getOldCode(), detail);
            }
        }

        List<String> unifiedCodes = new ArrayList<>();
        for (OrganizationMergeDetailDraft detail: detailDrafts) {
            OrganizationMergeDetail detailMer = organizationMergeDetailMap.getOrDefault(detail.getOldCode(), null);
            if (Objects.isNull(detailMer)) {
                detailMer = OrganizationMergeDetail.builder()
                        .oldCode(detail.getOldCode())
                        .oldName(detail.getOldName())
                        .referenceId(organizationMerge.getId())
                        .build();

                newDetailList.add(detailMer);
                unifiedCodes.add(detail.getOldCode());
            }
        }

        //reset những user hợp nhất nhầm khôi phục về tcd cũ
        resetOrgDv(removeList, orgCodeSetActive);

        //set lại trạng thái hoạt động cho TCD
        setStatusActiveOrg(orgCodeSetActive);

        Organization oldOrganization = organizationService.findByCode(organizationMerge.getOrganizationCode());

        String newOrganizationCode = organizationMerge.getOrganizationCode();
        if (Objects.equals(oldOrganization.getCode(), newOrganizationCode)) {
            oldOrganization.setName(organizationMerge.getOrganizationName());

            organizationService.save(oldOrganization);
            //chuyen cac dang vien tu TCD bi sap nhap/hop nhat sang TCD nhan sap nhap/hop nhat
            unifyOrganization(unifiedCodes, organizationMerge.getOrganizationCode());
            saveUpdate(organizationMergeDraft, organizationMerge, newDetailList);
            return true;
        }

        Organization organization = new Organization();
        organization.setCode(newOrganizationCode);
        organization.setName(organizationMerge.getOrganizationName());
        organization.setForm(organizationMerge.getForm());
        if (newOrganizationCode.length() == Constants.FORM_B_NAME_LENGTH) {
            organization.setParentCode(Constants.DANG_UY_AGRIBANK_CODE);
        }
        else{
            organization.setParentCode(newOrganizationCode.substring(0, newOrganizationCode.length() - 2));
        }
        organization.setStatus(EOrganizationStatus.YES.getStatus());

        //Lưu mã TCD mới và xóa mã TCD bị nhầm
        organizationClient.save(organization);
        organizationClient.deleteById(oldOrganization.getCode());

        //chuyen cac dang vien tu TCD bi sap nhap/hop nhat sang TCD nhan sap nhap/hop nhat
        unifyOrganization(unifiedCodes, organizationMerge.getOrganizationCode());
        saveUpdate(organizationMergeDraft, organizationMerge, newDetailList);
        return true;
    }

    private void resetOrgDv(List<OrganizationMergeDetail> removeList, List<String> orgCodeSetActive) {
        List<DvOrgHistory> dvOrgHistoryList = new ArrayList<>();
        if (!removeList.isEmpty()) {
            dvOrgHistoryList = dvOrgService.findByOldOrgCodeIn(orgCodeSetActive);
            mergeDetailClient.deleteAll(removeList);
        }

        if (!dvOrgHistoryList.isEmpty()) {
            List<String> staffCodes = dvOrgHistoryList.stream().map(DvOrgHistory::getStaffCode).toList();
            List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();
            Map<String, DvOrgHistory> dvOrgHistoryMap = new HashMap<>();

            for (DvOrgHistory dvOrgHistory : dvOrgHistoryList) {
                dvOrgHistoryMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }

            for (DV dv : dvs) {
                DvOrgHistory dvOrgHistory = dvOrgHistoryMap.getOrDefault(dv.getStaffCode(), null);
                if (Objects.nonNull(dvOrgHistory)) {
                    dv.setOrganizationCode(dvOrgHistory.getOldOrgCode());
                }
            }

            dvClient.saveAll(dvs);
        }
    }

    private void saveUpdate(OrganizationMergeDraft organizationMergeDraft,
                            OrganizationMerge organizationMerge,
                            List<OrganizationMergeDetail> newDetailList){
        organizationMergeDraft.setApprovedBy(getUserRequested().getId());
        organizationMergeDraft.setStatus(EApprovalStatus.APPROVED.getId());
        mergeDraftClient.save(organizationMergeDraft);
        mergeDetailClient.saveAll(newDetailList);
        mergeClient.save(organizationMerge);
    }

    //set lại trạng thái TCD hoạt động
    private void setStatusActiveOrg(List<String> orgCodeSetActive){
        if (!orgCodeSetActive.isEmpty()) {
            List<Organization> organizationList = organizationClient.findAllByCode(orgCodeSetActive).getData();
            for (Organization organization: organizationList) {
                organization.setStatus(EOrganizationStatus.YES.getStatus());
            }

            organizationClient.saveAll(organizationList);
        }
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {
        denyMergeRequest(referenceId);
    }
}
