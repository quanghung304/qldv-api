package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.UnifyOrganizationRequest;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.MergeFilterRequest;
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
public class OrganizationUnifyService implements EntityHandler {
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

    static final EForm form = EForm.BIEU_02_UNION;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationMergeDraft.FIELD_MAP_MERGE);

        return combinedFieldMap;
    }

    public Request createUnifyRequest(UnifyOrganizationRequest request) {

        List<Organization> unifiedOrganizations = organizationClient.findAllByCode(request.getUnifyCodes()).getData();
        unifiedOrganizations = unifiedOrganizations.stream()
                .filter(x -> Objects.equals(x.getStatus(), EOrganizationStatus.YES.getStatus()))
                .toList();

        if (unifiedOrganizations.isEmpty()) {
            throw new CommonException("Danh sách chi bộ nhận sáp nhập không hợp lệ");
        }

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
        String jsonData = createJsonData(organizationUnify, unifyDetails);
        unifyRequest.setNewData(jsonData);
        unifyRequest.setReferenceId(organizationUnify.getId());
        unifyRequest.setCreatedBy(userDetails.getId());
        unifyRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(unifyRequest);

        return unifyRequest;
    }

    private String createJsonData(OrganizationMergeDraft organizationUnify, List<OrganizationMergeDetailDraft> unifyDetails) {
        try {
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(organizationUnify, getCombinedFieldMap());
            Map<String, String> fieldMap = OrganizationMergeDetailDraft.FIELD_MAP_UNIFY;
            int i = 1;

            for (OrganizationMergeDetailDraft detail : unifyDetails) {
                BeanWrapper wrapper = new BeanWrapperImpl(detail);

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
        request.setType(EReport01Type.UNION.getId());
        return organizationMergeClient.getList(request).getData();
    }

    public OrganizationMergeResponse getDetail(String id) {
        OrganizationMerge organizationUnify = organizationMergeClient.findById(id).getData().orElse(null);

        if (Objects.isNull(organizationUnify)) {
            throw new CommonException("Không tìm thấy yêu cầu hợp nhất");
        }

        OrganizationMergeResponse response = modelMapper.map(organizationUnify, OrganizationMergeResponse.class);

        List<OrganizationMergeDetail> details = mergeDetailClient.findByRefId(id).getData();
        List<OrganizationMergeResponse.MergeDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetail detail: details) {
            OrganizationMergeResponse.MergeDetailResponse detailResponse = modelMapper.map(detail, OrganizationMergeResponse.MergeDetailResponse.class);
            detailResponses.add(detailResponse);
        }

        response.setMergeDetails(detailResponses);
        return response;
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
        organizationUnify = organizationMergeClient.save(organizationUnify).getData();

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

        //luu trang thai draft
        draft.setApprovedBy(userDetails.getStaffCode());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        mergeDraftClient.save(draft);

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

        return true;
    }

    private void unifyOrganization(List<String> unifiedCodes, String unifyingCode) {
        List<Organization> unifiedOrganizations = organizationClient.findAllByCode(unifiedCodes).getData();

        for(Organization organization: unifiedOrganizations) {
            organization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        List<DV> unifiedMembers = dvClient.findByOrganizationCodes(unifiedCodes).getData();
        for (DV member: unifiedMembers) {
            member.setOrganizationCode(unifyingCode);
        }

        organizationClient.saveAll(unifiedOrganizations);
        dvClient.saveAll(unifiedMembers);
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
