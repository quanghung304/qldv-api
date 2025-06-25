package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EOrganizationStatus;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.BaseFormEntity;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.MergeFilterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationMergeService implements EntityHandler {
    ModelMapper modelMapper;
    ObjectMapper objectMapper;

    DVClient dvClient;
    OrganizationClient organizationClient;
    OrganizationMergeClient organizationMergeClient;
    OrganizationMergeDetailClient mergeDetailClient;
    OrganizationMergeDraftClient mergeDraftClient;
    OrganizationMergeDetailDraftClient mergeDetailDraftClient;
    RequestClient requestClient;

    RequestService requestService;

    static EForm form = EForm.BIEU_02_MERGE;

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
        String jsonData = createJsonData(organizationMerge, mergeDetails);
        mergeRequest.setNewData(jsonData);
        mergeRequest.setReferenceId(organizationMerge.getId());
        mergeRequest.setCreatedBy(userDetails.getId());
        mergeRequest.setOrganizationCode(mergingOrganization.getCode());
        requestClient.save(mergeRequest);

        return mergeRequest;
    }

    private String createJsonData(OrganizationMergeDraft organizationMerge, List<OrganizationMergeDetailDraft> organizationMergeDetails) {
        try {
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(organizationMerge, getCombinedFieldMap());
            Map<String, String> fieldMap = OrganizationMergeDetailDraft.FIELD_MAP_MERGE;
            int i = 1;

            for (OrganizationMergeDetailDraft mergedOrganization : organizationMergeDetails) {
                BeanWrapper wrapper = new BeanWrapperImpl(mergedOrganization);

                // Iterate over fieldMap keys (entity fields)
                for (String fieldName : fieldMap.keySet()) {
                    if (wrapper.isReadableProperty(fieldName)) {
                        Object value = wrapper.getPropertyValue(fieldName);
                        draftDataMap.put(fieldMap.get(fieldName) + " " + i, value);
                    }
                }
                i++;
            }

            return objectMapper.writeValueAsString(draftDataMap);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<OrganizationMerge> getList(MergeFilterRequest request) {
        request.setType(EReport01Type.MERGE.getId());
        return organizationMergeClient.getList(request).getData();
    }

    public OrganizationMergeResponse getDetail(String id) {
        OrganizationMerge organizationMerge = organizationMergeClient.findById(id).getData().orElse(null);

        if (Objects.isNull(organizationMerge)) {
            throw new CommonException("Không tìm thấy yêu cầu sáp nhập");
        }

        OrganizationMergeResponse response = modelMapper.map(organizationMerge, OrganizationMergeResponse.class);

        List<OrganizationMergeDetail> mergeDetails = mergeDetailClient.findByRefId(id).getData();
        List<OrganizationMergeResponse.MergeDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetail mergeDetail: mergeDetails) {
            OrganizationMergeResponse.MergeDetailResponse detailResponse = modelMapper.map(mergeDetail, OrganizationMergeResponse.MergeDetailResponse.class);
            detailResponses.add(detailResponse);
        }

        response.setMergeDetails(detailResponses);
        return response;
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
        organizationMerge = organizationMergeClient.save(organizationMerge).getData();

        //luu danh sach chi nhanh dc sap nhap/hop nhat
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

        //chuyen cac dang vien tu TCD bi sap nhap/hop nhat sang TCD nhan sap nhap/hop nhat
        mergeOrganization(mergedCodes, organizationMerge.getOrganizationCode());

        return true;
    }

    private void mergeOrganization(List<String> mergedCodes, String mergingCode) {
        List<Organization> mergedOrganizations = organizationClient.findAllByCode(mergedCodes).getData();

        for(Organization organization: mergedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DV> mergedMembers = dvClient.findByOrganizationCodes(mergedCodes).getData();
        for (DV member: mergedMembers) {
            member.setOrganizationCode(mergingCode);
        }

        organizationClient.saveAll(mergedOrganizations);
        dvClient.saveAll(mergedMembers);
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        return false;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {
        OrganizationMergeDraft draft = mergeDraftClient.findById(referenceId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        draft.setStatus(EApprovalStatus.DENIED.getId());
        draft.setApprovedBy(userDetails.getId());
        mergeDraftClient.save(draft);
    }
}
