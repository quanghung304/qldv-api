package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EOrganizationStatus;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public Request createMergeRequest(MergeOrganizationRequest request) {
        Organization mergingOrganization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(mergingOrganization)) {
            throw new CommonException("Chi bộ nhận sáp nhập không tồn tại");
        }

        List<Organization> mergedOrganizations = organizationClient.findAllByCode(request.getMergedCodes()).getData();
        mergedOrganizations = mergedOrganizations.stream()
                .filter(x -> Objects.equals(x.getStatus(), EOrganizationStatus.YES.getStatus()))
                .toList();

        if (mergedOrganizations.isEmpty()) {
            throw new CommonException("Danh sách chi bộ nhận sáp nhập không hợp lệ");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft organizationMerge = modelMapper.map(request, OrganizationMergeDraft.class);
        organizationMerge.setType(EReport01Type.MERGE.getId());
        organizationMerge.setOrganizationName(mergingOrganization.getName());
        organizationMerge.setForm(mergingOrganization.getForm());
        organizationMerge.setCreatedBy(userDetails.getStaffCode());
        organizationMerge = mergeDraftClient.save(organizationMerge).getData();

        List<OrganizationMergeDetailDraft> mergeDetails = new ArrayList<>();
        for (Organization mergedOrganization: mergedOrganizations) {
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(organizationMerge.getId())
                    .oldCode(mergedOrganization.getCode())
                    .oldName(mergedOrganization.getName())
                    .build();

            mergeDetails.add(detail);
        }
        mergeDetailDraftClient.saveAll(mergeDetails);

        Request mergeRequest = requestService.initializeRequest(organizationMerge, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(organizationMerge, mergeDetails, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(jsonData);
        mergeRequest.setReferenceId(organizationMerge.getId());
        mergeRequest.setCreatedBy(userDetails.getId());
        mergeRequest.setOrganizationCode(mergingOrganization.getCode());
        requestClient.save(mergeRequest);

        return mergeRequest;
    }

    @Override
    public PageResponse<OrganizationMerResponse> getList(SearchOrganizationUnionRequest request){
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

        List<Organization> mergedOrganizations = organizationClient.findAllByCode(request.getMergedCodes()).getData();
        if (mergedOrganizations.isEmpty()) {
            throw new CommonException("Danh sách chi bộ nhận sáp nhập không hợp lệ");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationMergeDraft draft = modelMapper.map(request, OrganizationMergeDraft.class);
        draft.setType(EReport01Type.MERGE.getId());
        draft.setOrganizationName(mergingOrganization.getName());
        draft.setForm(mergingOrganization.getForm());
        draft.setCreatedBy(userDetails.getStaffCode());
        draft.setRefId(merge.getId());
        draft = mergeDraftClient.save(draft).getData();

        List<OrganizationMergeDetailDraft> draftDetails = new ArrayList<>();
        for (Organization mergedOrganization: mergedOrganizations) {
            OrganizationMergeDetailDraft detail = OrganizationMergeDetailDraft.builder()
                    .referenceId(draft.getId())
                    .oldCode(mergedOrganization.getCode())
                    .oldName(mergedOrganization.getName())
                    .build();

            draftDetails.add(detail);
        }
        mergeDetailDraftClient.saveAll(draftDetails);

        Request mergeRequest = requestService.initializeRequest(draft, merge, form, getCombinedFieldMap());

        String newData = createJsonData(draft, draftDetails, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setNewData(newData);
        String oldData = createJsonData(merge, mergeDetails, OrganizationMergeDetailDraft.FIELD_MAP_MERGE);
        mergeRequest.setOldData(oldData);

        mergeRequest.setReferenceId(draft.getId());
        mergeRequest.setCreatedBy(userDetails.getId());
        mergeRequest.setOrganizationCode(mergingOrganization.getCode());
        requestClient.save(mergeRequest);

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

        for (OrganizationMergeDetailDraft detailDraft: mergeDetailDrafts) {
            OrganizationMergeDetail mergeDetail = modelMapper.map(detailDraft, OrganizationMergeDetail.class);
            mergeDetail.setReferenceId(organizationMerge.getId());
            mergeDetails.add(mergeDetail);
            mergedCodes.add(detailDraft.getOldCode());
        }
        mergeDetailClient.saveAll(mergeDetails);

        //luu trang thai draft
        mergeDraft.setApprovedBy(userDetails.getStaffCode());
        mergeDraft.setStatus(EApprovalStatus.APPROVED.getId());
        mergeDraftClient.save(mergeDraft);

        //chuyen cac dang vien tu TCD bi sap nhap sang TCD nhan sap nhap
        mergeOrganization(organizationMerge.getOrganizationCode(),mergedCodes, organizationMerge.getId());

        return true;
    }

    private void mergeOrganization(String mergingCode, List<String> mergedCodes, String organizationMergeId) {
        List<Organization> mergedOrganizations = organizationClient.findAllByCode(mergedCodes).getData();

        for(Organization organization: mergedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DvOrgHistory> dvOrgHistories = new ArrayList<>();
        List<DV> mergedMembers = dvClient.findByOrganizationCodes(mergedCodes).getData();

        for (DV member: mergedMembers) {
            DvOrgHistory dvOrgHistory = DvOrgHistory.builder()
                    .staffCode(member.getStaffCode())
                    .oldOrgCode(member.getOrganizationCode())
                    .newOrgCode(mergingCode)
                    .refId(organizationMergeId)
                    .build();
            dvOrgHistories.add(dvOrgHistory);

            member.setOrganizationCode(mergingCode);
        }

        dvOrgService.saveAll(dvOrgHistories);
        organizationClient.saveAll(mergedOrganizations);
        dvClient.saveAll(mergedMembers);
    }

    @Override
    public boolean applyUpdate(String draftId) {
        OrganizationMergeDraft mergeDraft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(mergeDraft) || !Objects.equals(mergeDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Yêu cầu không hợp lệ");
        }

        List<OrganizationMergeDetailDraft> mergeDetailDrafts = mergeDetailDraftClient.findByRefId(draftId).getData();

        if (mergeDetailDrafts.isEmpty()) {
            return false;
        }

        List<String> newMergedCodes = new ArrayList<>();
        for (OrganizationMergeDetailDraft detailDraft: mergeDetailDrafts) {
            newMergedCodes.add(detailDraft.getOldCode());
        }

        OrganizationMerge organizationMerge = mergeClient.findById(mergeDraft.getRefId()).getData()
                .orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));

        List<OrganizationMergeDetail> oldMergeDetails = mergeDetailClient.findByRefId(organizationMerge.getId()).getData();

        List<String> oldMergedCodes = new ArrayList<>();
        for (OrganizationMergeDetail detail: oldMergeDetails) {
            oldMergedCodes.add(detail.getOldCode());
        }

        rollbackMergeAction(organizationMerge.getOrganizationCode(), oldMergedCodes, organizationMerge.getId());
        mergeOrganization(mergeDraft.getOrganizationCode(), newMergedCodes, organizationMerge.getId());

        return true;
    }

    private void rollbackMergeAction(String newCode, List<String> oldMergedCodes, String refId) {
        List<Organization> oldMergedOrganization = organizationClient.findAllByCode(oldMergedCodes).getData();

        for (Organization organization: oldMergedOrganization) {
            organization.setStatus(EOrganizationStatus.YES.getStatus());
        }

        List<DvOrgHistory> dvOrgHistories = dvOrgHistoryClient.findByNewOrgCodeAndRefId(newCode, refId).getData();
        Map<String, DvOrgHistory> dvOrgHistoryMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistory dvOrgHistory: dvOrgHistories) {
            dvOrgHistoryMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            staffCodes.add(dvOrgHistory.getStaffCode());
        }

        List<DV> dvList = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv: dvList) {
            if (Objects.equals(dv.getOrganizationCode(), newCode)) {
                String oldCode = dvOrgHistoryMap.get(dv.getStaffCode()).getOldOrgCode();
                dv.setOrganizationCode(oldCode);
            }
        }

        organizationClient.saveAll(oldMergedOrganization);
        dvClient.saveAll(dvList);
        dvOrgHistoryClient.deleteAll(dvOrgHistories);
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
