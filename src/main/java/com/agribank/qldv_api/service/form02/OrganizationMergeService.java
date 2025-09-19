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
import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
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
public class OrganizationMergeService extends MergeUnifyService implements EntityHandler {
    static EForm form = EForm.BIEU_02_MERGE;

    public OrganizationMergeService(
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
                mergeDetailDraftClient, requestClient, dvOrgHistoryClient, dvOrgHistoryDraftClient, organizationMergeDetailClient, organizationHistoryClient, requestService, organizationService, dvOrgService,
                checkAuthorityService
        );
    }

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationMergeDraft.FIELD_MAP_MERGE);

        return combinedFieldMap;
    }

    public Request createMergeRequest(MergeOrganizationRequest request) {
        Organization mergingOrganization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(mergingOrganization)) {
            throw new CommonException("Chi bộ nhận sáp nhập không tồn tại");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft organizationMergeDraft = modelMapper.map(request, OrganizationMergeDraft.class);
        organizationMergeDraft.setType(EReport01Type.MERGE.getId());
        organizationMergeDraft.setOrganizationName(mergingOrganization.getName());
        organizationMergeDraft.setForm(mergingOrganization.getForm());
        organizationMergeDraft.setCreatedBy(userDetails.getStaffCode());
        organizationMergeDraft = mergeDraftClient.save(organizationMergeDraft).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> mergeDetailDrafts = createMergeDetailAndDvOrgHistoryDrafts(request, organizationMergeDraft, draftRequest);

        Request mergeRequest = requestService.initializeRequest(organizationMergeDraft, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(organizationMergeDraft, mergeDetailDrafts, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(jsonData);
        mergeRequest.setReferenceId(organizationMergeDraft.getId());
        mergeRequest.setCreatedBy(userDetails.getId());
        mergeRequest.setOrganizationCode(mergingOrganization.getCode());

        MergeDraftRequest mergeDraftRequest = MergeDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(mergeRequest)
                .build();

        mergeClient.saveDraftEntities(mergeDraftRequest);

        return mergeRequest;
    }

    private List<OrganizationMergeDetailDraft> createMergeDetailAndDvOrgHistoryDrafts(MergeOrganizationRequest request, OrganizationMergeDraft organizationMergeDraft, MergeDraftTempRequest draftRequest){
        if (request.getDetailRequests().isEmpty()){
            return new ArrayList<>();
        }

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = new ArrayList<>();
        List<DvOrgHistoryDraft> membersDraft = new ArrayList<>();

        for (MergeOrganizationRequest.MergeDetailRequest detailRequest : request.getDetailRequests()) {
            Organization mergedOrganization = organizationClient.findByCode(detailRequest.getMergedCode()).getData();
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(organizationMergeDraft.getId())
                    .oldCode(mergedOrganization.getCode())
                    .oldName(mergedOrganization.getName())
                    .build();

            mergeDetailDrafts.add(detail);

            for (String staffCode : detailRequest.getStaffCodes()) {
                DvOrgHistoryDraft memDraft = new DvOrgHistoryDraft();
                memDraft.setStaffCode(staffCode);
                memDraft.setOldOrgCode(detailRequest.getMergedCode());
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
        request.setType(EReport01Type.MERGE.getId());
        return search(request);
    }

    public Request update(MergeOrganizationRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Trường id không được bỏ trống");
        }

        OrganizationMerge merge = mergeClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));

        List<OrganizationMergeDetail> mergeDetails = mergeDetailClient.findByRefId(merge.getId()).getData();

        Organization mergingOrganization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(mergingOrganization)) {
            throw new CommonException("Chi bộ nhận sáp nhập không tồn tại");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft draft = modelMapper.map(request, OrganizationMergeDraft.class);
        draft.setType(EReport01Type.MERGE.getId());
        draft.setOrganizationName(mergingOrganization.getName());
        draft.setForm(mergingOrganization.getForm());
        draft.setCreatedBy(userDetails.getStaffCode());
        draft.setRefId(merge.getId());
        draft = mergeDraftClient.save(draft).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> draftDetails = createMergeDetailAndDvOrgHistoryDrafts(request, draft, draftRequest);

        Request mergeRequest = requestService.initializeRequest(draft, merge, form, getCombinedFieldMap());

        String newData = createJsonData(draft, draftDetails, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(newData);
        String oldData = createJsonData(merge, mergeDetails, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setOldData(oldData);
        mergeRequest.setReferenceId(draft.getId());
        mergeRequest.setCreatedBy(userDetails.getId());
        mergeRequest.setOrganizationCode(mergingOrganization.getCode());

        MergeDraftRequest mergeDraftRequest = MergeDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(mergeRequest)
                .build();

        mergeClient.saveDraftEntities(mergeDraftRequest);

        return mergeRequest;
    }

    @Override
    @Transactional
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        OrganizationMergeDraft mergeDraft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(mergeDraft) || !Objects.equals(mergeDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Yêu cầu không hợp lệ");
        }

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        if (mergeDetailDrafts.isEmpty()) {
            return false;
        }

        //Luu ho so ban goc
        OrganizationMerge organizationMerge = modelMapper.map(mergeDraft, OrganizationMerge.class);
        organizationMerge = mergeClient.save(organizationMerge).getData();

        //luu danh sach chi nhanh dc sap nhap
        List<OrganizationMergeDetail> mergeDetails = new ArrayList<>();
        List<String> mergedCodes = new ArrayList<>();

        for (OrganizationMergeDetailDraft detailDraft : mergeDetailDrafts) {
            OrganizationMergeDetail mergeDetail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            mergeDetail.setReferenceId(organizationMerge.getId());
            mergeDetails.add(mergeDetail);
            mergedCodes.add(detailDraft.getOldCode());
        }

        //luu trang thai draft
        mergeDraft.setApprovedBy(userDetails.getStaffCode());
        mergeDraft.setStatus(EApprovalStatus.APPROVED.getId());

        //chuyen cac dang vien tu TCD bi sap nhap sang TCD nhan sap nhap
        List<Organization> mergedOrganizations = organizationClient.findAllByCode(mergedCodes).getData();

        for (Organization organization : mergedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DvOrgHistory> dvOrgHistories = new ArrayList<>();
        List<DV> mergedMembers = createDvOrgHistory(draftId, organizationMerge, dvOrgHistories, EReport01Type.MERGE.getId());

        for (DV member : mergedMembers) {
            DvOrgHistory dvOrgHistory = DvOrgHistory.builder()
                    .staffCode(member.getStaffCode())
                    .oldOrgCode(member.getOrganizationCode())
                    .newOrgCode(organizationMerge.getOrganizationCode())
                    .refId(organizationMerge.getId())
                    .build();

            dvOrgHistories.add(dvOrgHistory);
            member.setOrganizationCode(organizationMerge.getOrganizationCode());
        }

        List<OrganizationHistory> organizationHistories = new ArrayList<>();

        OrganizationHistory organizationHistory = new OrganizationHistory();
        organizationHistory.setCode(organizationMerge.getOrganizationCode());
        organizationHistory.setName(organizationMerge.getOrganizationName());
        organizationHistory.setRefId(organizationMerge.getId());
        organizationHistory.setEffectiveDate(organizationMerge.getEffectiveDate());
        organizationHistory.setType(EReport01Type.MERGE.getId());
        organizationHistories.add(organizationHistory);

        for (OrganizationMergeDetail organizationMergeDetail : mergeDetails) {
            OrganizationHistory organizationHistoryMerged = new OrganizationHistory();
            organizationHistoryMerged.setCode(organizationMergeDetail.getOldCode());
            organizationHistoryMerged.setName(organizationMergeDetail.getOldName());
            organizationHistoryMerged.setRefId(organizationMergeDetail.getId());
            organizationHistoryMerged.setEffectiveDate(organizationMerge.getEffectiveDate());
            organizationHistoryMerged.setType(EReport01Type.MERGE.getId());
            organizationHistories.add(organizationHistoryMerged);
        }

        ApproveMergeRequest request = ApproveMergeRequest.builder()
                .organizationMergeDetails(mergeDetails)
                .mergedOrganizations(mergedOrganizations)
                .mergeDraft(mergeDraft)
                .dvOrgHistories(dvOrgHistories)
                .mergedMembers(mergedMembers)
                .organizationHistories(organizationHistories)
                .build();

        mergeClient.saveEntities(request);

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        OrganizationMergeDraft mergeDraft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(mergeDraft) || !Objects.equals(mergeDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Yêu cầu không hợp lệ");
        }

        mergeDraft.setApprovedBy(getUserRequested().getId());
        mergeDraft.setStatus(EApprovalStatus.APPROVED.getId());

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        if (mergeDetailDrafts.isEmpty()) {
            return false;
        }

        OrganizationMerge organizationMerge = mergeClient.findById(mergeDraft.getRefId()).getData()
                .orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));

        organizationMerge.setEffectiveDate(mergeDraft.getEffectiveDate());
        organizationMerge.setDecisionCommittee(mergeDraft.getDecisionCommittee());
        organizationMerge.setDecisionDate(mergeDraft.getDecisionDate());
        organizationMerge.setDecisionNumber(mergeDraft.getDecisionNumber());
        organizationMerge.setConclusionDate(mergeDraft.getConclusionDate());
        organizationMerge.setConclusionNumber(mergeDraft.getConclusionNumber());

        //Lấy danh sách OrganizationMergeDetail mới
        List<String> newMergedCodes = new ArrayList<>();
        List<OrganizationMergeDetail> newDetailList = new ArrayList<>();

        for (OrganizationMergeDetailDraft detailDraft : mergeDetailDrafts) {
            OrganizationMergeDetail organizationMergeDetail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            organizationMergeDetail.setReferenceId(organizationMerge.getId());
            newDetailList.add(organizationMergeDetail);
            newMergedCodes.add(detailDraft.getOldCode());
        }

        List<OrganizationMergeDetail> oldMergeDetails = mergeDetailClient.findByRefId(organizationMerge.getId()).getData();
        List<String> oldMergedCodes = new ArrayList<>();

        for (OrganizationMergeDetail detail : oldMergeDetails) {
            oldMergedCodes.add(detail.getOldCode());
        }

        List<Organization> oldMergedOrganizations = organizationClient.findAllByCode(oldMergedCodes).getData();

        for (Organization organization : oldMergedOrganizations) {
            organization.setStatus(EOrganizationStatus.YES.getStatus());
        }

        //lay danh sach cac dang vien luu OrganizationCode truoc khi update
        List<DvOrgHistory> oldDvOrgHistories = dvOrgHistoryClient.findByNewOrgCodeAndRefId(organizationMerge.getOrganizationCode(), organizationMerge.getId()).getData();
        List<DV> oldDVs = rollBackDV(draftId, organizationMerge, oldDvOrgHistories);

        //lay danh sach cac dang vien luu OrganizationCode sau khi update
        List<Organization> newMergedOrganizations = organizationClient.findAllByCode(newMergedCodes).getData();

        for (Organization organization : newMergedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DvOrgHistory> newDvOrgHistories = new ArrayList<>();
        List<DV> newDVs = createDvOrgHistory(draftId, organizationMerge, newDvOrgHistories, EReport01Type.MERGE.getId());

        //update lai EffectiveDate o bang OrganizationHistory
        List<OrganizationHistory> newOrganizationHistories = new ArrayList<>();
        OrganizationHistory organizationHistory = organizationHistoryClient.findByRefId(EReport01Type.MERGE.getId(), organizationMerge.getId()).getData();
        organizationHistory.setEffectiveDate(mergeDraft.getEffectiveDate());
        newOrganizationHistories.add(organizationHistory);

        for (OrganizationMergeDetail organizationMergeDetail : newDetailList) {
            OrganizationHistory organizationHistoryMerged = new OrganizationHistory();
            organizationHistoryMerged.setCode(organizationMergeDetail.getOldCode());
            organizationHistoryMerged.setName(organizationMergeDetail.getOldName());
            organizationHistoryMerged.setRefId(organizationMergeDetail.getId());
            organizationHistoryMerged.setEffectiveDate(organizationMerge.getEffectiveDate());
            organizationHistoryMerged.setType(EReport01Type.MERGE.getId());
            newOrganizationHistories.add(organizationHistory);
        }

        List<OrganizationHistory> oldOrganizationHistories = new ArrayList<>();

        for (OrganizationMergeDetail organizationMergeDetail : oldMergeDetails) {
            OrganizationHistory oldOrganizationHistory = organizationHistoryClient.findByRefId(EReport01Type.MERGE.getId(), organizationMergeDetail.getId()).getData();
            oldOrganizationHistories.add(oldOrganizationHistory);
        }

        ApproveUpdateMergeRequest request = ApproveUpdateMergeRequest.builder()
                .newOrganizationMergeDetails(newDetailList)
                .oldOrganizationMergeDetails(oldMergeDetails)
                .oldMergedOrganizations(oldMergedOrganizations)
                .newMergedOrganizations(newMergedOrganizations)
                .organizationMerge(organizationMerge)
                .mergeDraft(mergeDraft)
                .oldDvOrgHistories(oldDvOrgHistories)
                .newDvOrgHistories(newDvOrgHistories)
                .oldDVs(oldDVs)
                .newDVs(newDVs)
                .newOrganizationHistories(newOrganizationHistories)
                .oldOrganizationHistories(oldOrganizationHistories)
                .build();

        mergeClient.updateEntities(request);

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

    public OrganizationMergeResponse getDraftDetail(String draftId) {
        OrganizationMergeDraft mergeDraft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(mergeDraft)) {
            throw new CommonException("Không tìm thấy yêu cầu sáp nhập");
        }

        OrganizationMergeResponse response = modelMapper.map(mergeDraft, OrganizationMergeResponse.class);
        Organization mergeOrganization = organizationClient.findByCode(mergeDraft.getOrganizationCode()).getData();
        response.setOrganizationMerge(mergeOrganization);

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        List<String> organizations = new ArrayList<>();
        for (OrganizationMergeDetailDraft mergeDetailDraft: mergeDetailDrafts) {
            organizations.add(mergeDetailDraft.getOldCode());
        }

        List<Organization> oldOrganizationDrafts = organizationClient.findAllByCode(organizations).getData();

        Map<String, Organization> oldOrganizationDraftMaps = new HashMap<>();

        for (Organization organization : oldOrganizationDrafts) {
            oldOrganizationDraftMaps.put(organization.getCode(), organization);
        }

        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(draftId).getData();

        Map<String, List<DvOrgHistoryDraft>> dvOrgHistoryDraftMap = dvOrgHistoryDrafts.stream()
                .collect(Collectors.groupingBy(DvOrgHistoryDraft::getOldOrgCode));

//        if (dvOrgHistoryDraftMap.isEmpty()) {
//            throw new CommonException("Không có dữ liệu đảng bộ chia tách");
//        }

        Map<String, DV> dvOfOldOrgMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistoryDraft dvOrgHistoryDraft : dvOrgHistoryDrafts) {
            staffCodes.add(dvOrgHistoryDraft.getStaffCode());
        }
        List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : dvs) {
            dvOfOldOrgMap.put(dv.getStaffCode(), dv);
        }

        List<OrganizationMergeResponse.MergeDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetailDraft mergeDetailDraft: mergeDetailDrafts) {
            OrganizationMergeResponse.MergeDetailResponse detailResponse = new OrganizationMergeResponse.MergeDetailResponse();
            List<DvOrgHistoryDraft> items = dvOrgHistoryDraftMap.get(mergeDetailDraft.getOldCode());
            List<DV> dvsOfOldOrg = new ArrayList<>();

            if (!Objects.isNull(items)) {
                for (DvOrgHistoryDraft dvOrgHistoryDraft : items) {
                    dvsOfOldOrg.add(dvOfOldOrgMap.get(dvOrgHistoryDraft.getStaffCode()));
                }
            }

            detailResponse.setOrganization(oldOrganizationDraftMaps.get(mergeDetailDraft.getOldCode()));
            detailResponse.setMembers(dvsOfOldOrg);
            detailResponses.add(detailResponse);
        }

        response.setMergeDetails(detailResponses);
        return response;
    }

    public String updateDraft(MergeOrganizationRequest request) {
        OrganizationMergeDraft mergeDraft = mergeDraftClient.findById(request.getId()).getData().orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));;

        if (!Objects.equals(mergeDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Chỉ được chỉnh sửa yêu cầu chưa được phê duyệt");
        }

        Organization mergeOrganization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(mergeOrganization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        mergeDraft.setOrganizationCode(request.getOrganizationCode());
        mergeDraft.setOrganizationName(mergeOrganization.getName());
        mergeDraft.setForm(mergeOrganization.getForm());
        mergeDraft.setDecisionDate(request.getDecisionDate());
        mergeDraft.setConclusionNumber(request.getConclusionNumber());
        mergeDraft.setDecisionNumber(request.getDecisionNumber());
        mergeDraft.setConclusionDate(request.getConclusionDate());
        mergeDraft.setDecisionCommittee(request.getDecisionCommittee());
        mergeDraft.setEffectiveDate(request.getEffectiveDate());

        List<OrganizationMergeDetailDraft> oldOrganizationMergeDetailDrafts = mergeDetailDraftClient.findByRefId(mergeDraft.getId()).getData();

        if (oldOrganizationMergeDetailDrafts.isEmpty()) {
            throw new CommonException("Không tìm thấy thông tin tổ chức Đảng bị sáp nhâp");
        }

        List<DvOrgHistoryDraft> oldDvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(mergeDraft.getId()).getData();

        MergeDraftTempRequest draftRequest = new MergeDraftTempRequest();
        List<OrganizationMergeDetailDraft> mergeDetailDrafts = createMergeDetailAndDvOrgHistoryDrafts(request, mergeDraft, draftRequest);

        Request mergeRequest = requestService.getRequestByDraftId(form.getCode(), request.getId());
        String jsonData = createJsonData(mergeDraft, mergeDetailDrafts, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(jsonData);
        mergeRequest.setOrganizationCode(request.getOrganizationCode());

        MergeUpdateDraftRequest mergeUpdateDraftRequest = MergeUpdateDraftRequest.builder()
                .mergeDetailDrafts(draftRequest.getMergeDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .mergeRequest(mergeRequest)
                .mergeDraft(mergeDraft)
                .oldOrganizationMergeDetailDrafts(oldOrganizationMergeDetailDrafts)
                .oldDvOrgHistoryDrafts(oldDvOrgHistoryDrafts)
                .build();

        mergeClient.saveUpdateDraftEntities(mergeUpdateDraftRequest);

        return "Cập nhật yêu cầu thành công";
    }
}
