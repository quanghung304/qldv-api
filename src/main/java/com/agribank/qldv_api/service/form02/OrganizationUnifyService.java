package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.MergeDraftTempRequest;
import com.agribank.qldv_api.request.form02.UnifyOrgUpdateRequest;
import com.agribank.qldv_api.request.form02.UnifyOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationUnifyResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.DvOrgService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.*;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
            DvOrgHistoryDraftClient dvOrgHistoryDraftClient,
            OrganizationMergeDetailClient organizationMergeDetailClient,
            OrganizationHistoryClient organizationHistoryClient,
            RequestService requestService,
            OrganizationService organizationService,
            DvOrgService dvOrgService,
            CheckAuthorityService checkAuthorityService
    ) {
        super(
                objectMapper, modelMapper, dvClient, organizationClient, mergeClient, mergeDetailClient, mergeDraftClient,
                mergeDetailDraftClient, requestClient, dvOrgHistoryClient, dvOrgHistoryDraftClient, organizationMergeDetailClient, organizationHistoryClient,
                requestService, organizationService, dvOrgService, checkAuthorityService
        );
    }

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationMergeDraft.FIELD_MAP_MERGE);

        return combinedFieldMap;
    }

    public Request createUnifyRequest(UnifyOrganizationRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft organizationUnify = modelMapper.map(request, OrganizationMergeDraft.class);
        organizationUnify.setType(EReport01Type.UNION.getId());
        organizationUnify.setCreatedBy(userDetails.getStaffCode());
        organizationUnify = mergeDraftClient.save(organizationUnify).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> unifyDetailDrafts = createMergeDetailAndDvOrgHistoryDrafts(request, organizationUnify, draftRequest);

        Request unifyRequest = requestService.initializeRequest(organizationUnify, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(organizationUnify, unifyDetailDrafts, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        unifyRequest.setNewData(jsonData);
        unifyRequest.setReferenceId(organizationUnify.getId());
        unifyRequest.setCreatedBy(userDetails.getId());
        unifyRequest.setOrganizationCode(request.getOrganizationCode());

        MergeDraftRequest mergeDraftRequest = MergeDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(unifyRequest)
                .build();

        mergeClient.saveDraftEntities(mergeDraftRequest);

        return unifyRequest;
    }

    private List<OrganizationMergeDetailDraft> createMergeDetailAndDvOrgHistoryDrafts(UnifyOrganizationRequest request, OrganizationMergeDraft organizationMergeDraft, MergeDraftTempRequest draftRequest){
        if (request.getDetailRequests().isEmpty()){
            return new ArrayList<>();
        }

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = new ArrayList<>();
        List<DvOrgHistoryDraft> membersDraft = new ArrayList<>();

        for (UnifyOrganizationRequest.UnifyDetailRequest detailRequest : request.getDetailRequests()) {
            Organization mergedOrganization = organizationClient.findByCode(detailRequest.getUnifiedCode()).getData();
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(organizationMergeDraft.getId())
                    .oldCode(mergedOrganization.getCode())
                    .oldName(mergedOrganization.getName())
                    .build();

            mergeDetailDrafts.add(detail);

            for (String staffCode : detailRequest.getStaffCodes()) {
                DvOrgHistoryDraft memDraft = new DvOrgHistoryDraft();
                memDraft.setStaffCode(staffCode);
                memDraft.setOldOrgCode(detailRequest.getUnifiedCode());
                memDraft.setNewOrgCode(request.getOrganizationCode());
                memDraft.setRefId(organizationMergeDraft.getId());
                membersDraft.add(memDraft);
            }
        }

        draftRequest.setMembersDraft(membersDraft);
        draftRequest.setMergeDetailDrafts(mergeDetailDrafts);

        return mergeDetailDrafts;
    }

    @Override
    public PageResponse<OrganizationMerResponse> getList(SearchOrganizationUnionRequest request) {
        request.setType(EReport01Type.UNION.getId());
        return search(request);
    }

    public RequestResponse update(UnifyOrgUpdateRequest request) {
        OrganizationMerge oldUnify = mergeClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu. Vui lòng kiểm tra lại"));

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

        Request unifyRequest = requestService.initializeRequest(organizationUnify, oldUnify, form, getCombinedFieldMap());

        List<OrganizationMergeDetail> oldDetails = mergeDetailClient.findByRefId(oldUnify.getId()).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> unifyDetailDrafts = createMergeDetailAndDvOrgHistoryDrafts(request, organizationUnify, draftRequest);

        String newJsonData = createJsonData(organizationUnify, unifyDetailDrafts, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        String oldJsonData = createJsonData(oldUnify, oldDetails, OrganizationMergeDetailDraft.FIELD_MAP_UNIFY);
        unifyRequest.setNewData(newJsonData);
        unifyRequest.setOldData(oldJsonData);

        unifyRequest.setReferenceId(organizationUnify.getId());
        unifyRequest.setCreatedBy(userRequested.getId());
        unifyRequest.setOrganizationCode(request.getOrganizationCode());

        MergeDraftRequest mergeDraftRequest = MergeDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(unifyRequest)
                .build();

        mergeClient.saveDraftEntities(mergeDraftRequest);

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

        for (OrganizationMergeDetailDraft detailDraft : detailDrafts) {
            OrganizationMergeDetail detail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            detail.setReferenceId(organizationUnify.getId());
            details.add(detail);
            unifiedCodes.add(detailDraft.getOldCode());
        }

        Organization newOrganization = new Organization();
        String newOrganizationCode = organizationUnify.getOrganizationCode();
        newOrganization.setCode(newOrganizationCode);
        newOrganization.setName(organizationUnify.getOrganizationName());
        newOrganization.setForm(organizationUnify.getForm());

        if (newOrganizationCode.length() == Constants.FORM_B_NAME_LENGTH) {
            newOrganization.setParentCode(Constants.DANG_UY_AGRIBANK_CODE);
        } else {
            newOrganization.setParentCode(newOrganizationCode.substring(0, newOrganizationCode.length() - 2));
        }

        //chuyen cac dang vien tu TCD bi sap nhap/hop nhat sang TCD nhan sap nhap/hop nhat
        List<Organization> unifiedOrganizations = organizationClient.findAllByCode(unifiedCodes).getData();

        for (Organization organization : unifiedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DvOrgHistory> dvOrgHistories = new ArrayList<>();
        List<DV> unifiedMembers = createDvOrgHistory(draftId, organizationUnify, dvOrgHistories, EReport01Type.UNION.getId());

        //luu trang thai draft
        draft.setApprovedBy(userDetails.getStaffCode());
        draft.setStatus(EApprovalStatus.APPROVED.getId());

        List<OrganizationHistory> organizationHistories = new ArrayList<>();

        OrganizationHistory organizationHistory = new OrganizationHistory();
        organizationHistory.setCode(organizationUnify.getOrganizationCode());
        organizationHistory.setName(organizationUnify.getOrganizationName());
        organizationHistory.setRefId(organizationUnify.getId());
        organizationHistory.setEffectiveDate(organizationUnify.getEffectiveDate());
        organizationHistory.setType(EReport01Type.UNION.getId());
        organizationHistories.add(organizationHistory);

        for (OrganizationMergeDetail organizationMergeDetail : details) {
            OrganizationHistory organizationHistoryUnified = new OrganizationHistory();
            organizationHistoryUnified.setCode(organizationMergeDetail.getOldCode());
            organizationHistoryUnified.setName(organizationMergeDetail.getOldName());
            organizationHistoryUnified.setRefId(organizationMergeDetail.getId());
            organizationHistoryUnified.setEffectiveDate(organizationUnify.getEffectiveDate());
            organizationHistoryUnified.setType(EReport01Type.UNION.getId());
            organizationHistories.add(organizationHistoryUnified);
        }

        ApproveUnifyRequest request = ApproveUnifyRequest.builder()
                .organizationMergeDetails(details)
                .mergedOrganizations(unifiedOrganizations)
                .mergeDraft(draft)
                .dvOrgHistories(dvOrgHistories)
                .mergedMembers(unifiedMembers)
                .organizationHistories(organizationHistories)
                .newOrganization(newOrganization)
                .build();

        mergeClient.saveUnifyEntities(request);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        OrganizationMergeDraft organizationMergeDraft = mergeDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        organizationMergeDraft.setApprovedBy(getUserRequested().getId());
        organizationMergeDraft.setStatus(EApprovalStatus.APPROVED.getId());

        List<OrganizationMergeDetailDraft> detailDrafts = mergeDetailDraftClient.findByRefId(organizationMergeDraft.getId()).getData();
        if (detailDrafts.isEmpty()) {
            return false;
        }

        OrganizationMerge organizationUnify = mergeClient.findById(organizationMergeDraft.getRefId())
                .getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationMergeDetail> organizationMergeDetails = mergeDetailClient.findByRefId(organizationUnify.getId()).getData();
        if (organizationMergeDetails.isEmpty()) {
            return false;
        }

        String newOrganizationCode = organizationMergeDraft.getOrganizationCode();

        Organization newOrganization = new Organization();
        newOrganization.setCode(newOrganizationCode);
        newOrganization.setName(organizationMergeDraft.getOrganizationName());
        newOrganization.setForm(organizationMergeDraft.getForm());

        if (newOrganizationCode.length() == Constants.FORM_B_NAME_LENGTH) {
            newOrganization.setParentCode(Constants.DANG_UY_AGRIBANK_CODE);
        } else {
            newOrganization.setParentCode(newOrganizationCode.substring(0, newOrganizationCode.length() - 2));
        }

        newOrganization.setStatus(EOrganizationStatus.YES.getStatus());

        Organization oldOrganization = organizationService.findByCode(organizationUnify.getOrganizationCode());

        //Lấy danh sách OrganizationMergeDetail mới
        List<String> newMergedCodes = new ArrayList<>();
        List<OrganizationMergeDetail> newDetailList = new ArrayList<>();

        for (OrganizationMergeDetailDraft detailDraft : detailDrafts) {
            OrganizationMergeDetail organizationMergeDetail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            organizationMergeDetail.setReferenceId(organizationUnify.getId());
            newDetailList.add(organizationMergeDetail);
            newMergedCodes.add(detailDraft.getOldCode());
        }

        List<OrganizationMergeDetail> oldMergeDetails = mergeDetailClient.findByRefId(organizationUnify.getId()).getData();
        List<String> oldMergedCodes = new ArrayList<>();

        for (OrganizationMergeDetail detail : oldMergeDetails) {
            oldMergedCodes.add(detail.getOldCode());
        }

        List<Organization> oldMergedOrganizations = organizationClient.findAllByCode(oldMergedCodes).getData();

        for (Organization organization : oldMergedOrganizations) {
            organization.setStatus(EOrganizationStatus.YES.getStatus());
        }

        //lay danh sach cac dang vien luu OrganizationCode truoc khi update
        List<DvOrgHistory> oldDvOrgHistories = dvOrgHistoryClient.findByNewOrgCodeAndRefId(organizationUnify.getOrganizationCode(), organizationUnify.getId()).getData();
        List<DV> oldDVs = rollBackDV(referenceId, organizationUnify, oldDvOrgHistories);

        //lay danh sach cac dang vien luu OrganizationCode sau khi update
        List<Organization> newMergedOrganizations = organizationClient.findAllByCode(newMergedCodes).getData();

        for (Organization organization : newMergedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DvOrgHistory> newDvOrgHistories = new ArrayList<>();
        List<DV> newDVs = createDvOrgHistory(referenceId, organizationUnify, newDvOrgHistories, EReport01Type.UNION.getId());

        //update lai EffectiveDate bang OrganizationHistory
        List<OrganizationHistory> newOrganizationHistories = new ArrayList<>();
        OrganizationHistory organizationHistory = organizationHistoryClient.findByRefId(EReport01Type.UNION.getId(), organizationUnify.getId()).getData();
        organizationHistory.setEffectiveDate(organizationMergeDraft.getEffectiveDate());
        newOrganizationHistories.add(organizationHistory);

        for (OrganizationMergeDetail organizationMergeDetail : newDetailList) {
            OrganizationHistory organizationHistoryUnified = new OrganizationHistory();
            organizationHistoryUnified.setCode(organizationMergeDetail.getOldCode());
            organizationHistoryUnified.setName(organizationMergeDetail.getOldName());
            organizationHistoryUnified.setRefId(organizationMergeDetail.getId());
            organizationHistoryUnified.setEffectiveDate(organizationUnify.getEffectiveDate());
            organizationHistoryUnified.setType(EReport01Type.UNION.getId());
            newOrganizationHistories.add(organizationHistoryUnified);
        }

        List<OrganizationHistory> oldOrganizationHistories = new ArrayList<>();

        for (OrganizationMergeDetail organizationMergeDetail : oldMergeDetails) {
            OrganizationHistory oldOrganizationHistory = organizationHistoryClient.findByRefId(EReport01Type.UNION.getId(), organizationMergeDetail.getId()).getData();
            if (Objects.nonNull(oldOrganizationHistory)) {
                oldOrganizationHistories.add(oldOrganizationHistory);
            }
        }

        organizationUnify.setForm(organizationMergeDraft.getForm());
        organizationUnify.setOrganizationCode(organizationMergeDraft.getOrganizationCode());
        organizationUnify.setOrganizationName(organizationMergeDraft.getOrganizationName());
        organizationUnify.setEffectiveDate(organizationMergeDraft.getEffectiveDate());
        organizationUnify.setDecisionCommittee(organizationMergeDraft.getDecisionCommittee());
        organizationUnify.setDecisionDate(organizationMergeDraft.getDecisionDate());
        organizationUnify.setDecisionNumber(organizationMergeDraft.getDecisionNumber());
        organizationUnify.setConclusionDate(organizationMergeDraft.getConclusionDate());
        organizationUnify.setConclusionNumber(organizationMergeDraft.getConclusionNumber());

        ApproveUpdateUnifyRequest request = ApproveUpdateUnifyRequest.builder()
                .newOrganizationMergeDetails(newDetailList)
                .oldOrganizationMergeDetails(oldMergeDetails)
                .oldMergedOrganizations(oldMergedOrganizations)
                .newMergedOrganizations(newMergedOrganizations)
                .organizationMerge(organizationUnify)
                .mergeDraft(organizationMergeDraft)
                .oldDvOrgHistories(oldDvOrgHistories)
                .newDvOrgHistories(newDvOrgHistories)
                .newOrganizationHistories(newOrganizationHistories)
                .oldOrganizationHistories(oldOrganizationHistories)
                .oldDVs(oldDVs)
                .newDVs(newDVs)
                .newOrganization(newOrganization)
                .oldOrganization(oldOrganization)
                .build();

        mergeClient.updateUnifyEntities(request);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {
        denyMergeRequest(referenceId);
    }

    public OrganizationUnifyResponse getDraftDetail(String draftId) {
        OrganizationMergeDraft unifyDraft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(unifyDraft)) {
            throw new CommonException("Không tìm thấy yêu cầu hợp nhất");
        }

        OrganizationUnifyResponse response = modelMapper.map(unifyDraft, OrganizationUnifyResponse.class);

        List<OrganizationMergeDetailDraft> unifyDetailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        List<String> organizations = new ArrayList<>();
        for (OrganizationMergeDetailDraft unifyDetailDraft: unifyDetailDrafts) {
            organizations.add(unifyDetailDraft.getOldCode());
        }

        List<Organization> oldOrganizationDrafts = organizationClient.findAllByCode(organizations).getData();

        Map<String, Organization> oldOrganizationDraftMaps = new HashMap<>();

        for (Organization organization : oldOrganizationDrafts) {
            oldOrganizationDraftMaps.put(organization.getCode(), organization);
        }

        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(draftId).getData();

        Map<String, List<DvOrgHistoryDraft>> dvOrgHistoryDraftMap = dvOrgHistoryDrafts.stream()
                .collect(Collectors.groupingBy(DvOrgHistoryDraft::getOldOrgCode));

        if (dvOrgHistoryDraftMap.isEmpty()) {
            throw new CommonException("Không có dữ liệu đảng bộ hợp nhất");
        }

        Map<String, DV> dvOfOldOrgMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistoryDraft dvOrgHistoryDraft : dvOrgHistoryDrafts) {
            staffCodes.add(dvOrgHistoryDraft.getStaffCode());
        }
        List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : dvs) {
            dvOfOldOrgMap.put(dv.getStaffCode(), dv);
        }

        List<OrganizationUnifyResponse.UnifyDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetailDraft unifyDetailDraft: unifyDetailDrafts) {
            OrganizationUnifyResponse.UnifyDetailResponse detailResponse = new OrganizationUnifyResponse.UnifyDetailResponse();
            List<DvOrgHistoryDraft> items = dvOrgHistoryDraftMap.get(unifyDetailDraft.getOldCode());
            List<DV> dvsOfOldOrg = new ArrayList<>();

            if (!Objects.isNull(items)) {
                for (DvOrgHistoryDraft dvOrgHistoryDraft : items) {
                    dvsOfOldOrg.add(dvOfOldOrgMap.get(dvOrgHistoryDraft.getStaffCode()));
                }
            }

            detailResponse.setOrganization(oldOrganizationDraftMaps.get(unifyDetailDraft.getOldCode()));
            detailResponse.setMembers(dvsOfOldOrg);
            detailResponses.add(detailResponse);
        }

        response.setUnifyDetails(detailResponses);
        return response;
    }

    public String updateDraft(UnifyOrgUpdateRequest request) {
        OrganizationMergeDraft unifyDraft = mergeDraftClient.findById(request.getId()).getData().orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));;

        if (!Objects.equals(unifyDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Chỉ được chỉnh sửa yêu cầu chưa được phê duyệt");
        }

        unifyDraft.setOrganizationCode(request.getOrganizationCode());
        unifyDraft.setOrganizationName(request.getOrganizationName());
        unifyDraft.setForm(request.getForm());
        unifyDraft.setDecisionDate(request.getDecisionDate());
        unifyDraft.setConclusionNumber(request.getConclusionNumber());
        unifyDraft.setDecisionNumber(request.getDecisionNumber());
        unifyDraft.setConclusionDate(request.getConclusionDate());
        unifyDraft.setEffectiveDate(request.getEffectiveDate());
        unifyDraft.setDecisionCommittee(request.getDecisionCommittee());

        List<OrganizationMergeDetailDraft> oldOrganizationUnifyDetailDrafts = mergeDetailDraftClient.findByRefId(unifyDraft.getId()).getData();
        if (oldOrganizationUnifyDetailDrafts.isEmpty()) {
            throw new CommonException("Không tìm thấy thông tin tổ chức Đảng bị sáp nhâp");
        }

        List<DvOrgHistoryDraft> oldDvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(unifyDraft.getId()).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> unifyDetailDrafts = createMergeDetailAndDvOrgHistoryDrafts(request, unifyDraft, draftRequest);

        Request mergeRequest = requestService.getRequestByDraftId(form.getCode(), request.getId());
        String jsonData = createJsonData(unifyDraft, unifyDetailDrafts, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(jsonData);
        mergeRequest.setOrganizationCode(request.getOrganizationCode());

        MergeUpdateDraftRequest mergeUpdateDraftRequest = MergeUpdateDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(mergeRequest)
                .mergeDraft(unifyDraft)
                .oldOrganizationMergeDetailDrafts(oldOrganizationUnifyDetailDrafts)
                .oldDvOrgHistoryDrafts(oldDvOrgHistoryDrafts)
                .build();

        mergeClient.saveUpdateDraftEntities(mergeUpdateDraftRequest);

        return "Cập nhật yêu cầu thành công";
    }
}
