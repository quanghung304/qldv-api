package com.agribank.qldv_api.service.form02;


import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.merge.OrganizationMergeDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.DvOrgService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.OrganizationDto;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class MergeUnifyService {
    ObjectMapper objectMapper;
    ModelMapper modelMapper;

    DVClient dvClient;
    OrganizationClient organizationClient;
    OrganizationMergeClient mergeClient;
    OrganizationMergeDetailClient mergeDetailClient;
    OrganizationMergeDraftClient mergeDraftClient;
    OrganizationMergeDetailDraftClient mergeDetailDraftClient;
    RequestClient requestClient;
    DvOrgHistoryClient dvOrgHistoryClient;


    RequestService requestService;
    OrganizationService organizationService;
    DvOrgService dvOrgService;
    CheckAuthorityService checkAuthorityService;


    abstract Map<String, String> getCombinedFieldMap();
    abstract PageResponse<OrganizationMerResponse> getList(SearchOrganizationUnionRequest request);

    public PageResponse<OrganizationMerResponse> search(SearchOrganizationUnionRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<OrganizationMerge> organizationMergePageResponse = mergeClient.searchUnion(request).getData();

        PageResponse<OrganizationMerResponse> response = new PageResponse<>();
        if (Objects.isNull(organizationMergePageResponse)) {
            return response;
        }

        response.setCurrentPage(organizationMergePageResponse.getCurrentPage());
        response.setTotalPages(organizationMergePageResponse.getTotalPages());
        response.setTotalItems(organizationMergePageResponse.getTotalItems());
        response.setData(organizationMergePageResponse.getData()
                .stream()
                .map(o -> modelMapper.map(o, OrganizationMerResponse.class)).toList());

        return response;
    }

    public OrganizationMergeResponse getDetail(String id) {
        OrganizationMerge organizationMerge = mergeClient.findById(id).getData().orElse(null);

        if (Objects.isNull(organizationMerge)) {
            throw new CommonException("Không tìm thấy yêu cầu sáp nhập");
        }

        OrganizationMergeResponse response = modelMapper.map(organizationMerge, OrganizationMergeResponse.class);

        Organization organization = organizationService.findByCode(organizationMerge.getDecisionCommittee());
        if (Objects.nonNull(organization)) {
            response.setDecision(modelMapper.map(organization, OrganizationDto.class));
        }
        List<OrganizationMergeDetail> mergeDetails = mergeDetailClient.findByRefId(id).getData();
        List<OrganizationMergeResponse.MergeDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetail mergeDetail: mergeDetails) {
            OrganizationMergeResponse.MergeDetailResponse detailResponse = modelMapper.map(mergeDetail, OrganizationMergeResponse.MergeDetailResponse.class);
            detailResponses.add(detailResponse);
        }

        response.setMergeDetails(detailResponses);
        return response;
    }

    String createJsonData(Object organizationMerge, List<?> organizationMergeDetails, Map<String, String> detailFieldMap) {
        try {
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(organizationMerge, getCombinedFieldMap());
            int i = 1;

            for (Object mergedOrganization : organizationMergeDetails) {
                BeanWrapper wrapper = new BeanWrapperImpl(mergedOrganization);

                // Iterate over fieldMap keys (entity fields)
                for (String fieldName : detailFieldMap.keySet()) {
                    if (wrapper.isReadableProperty(fieldName)) {
                        Object value = wrapper.getPropertyValue(fieldName);
                        draftDataMap.put(detailFieldMap.get(fieldName) + " " + i, value);
                    }
                }
                i++;
            }

            return objectMapper.writeValueAsString(draftDataMap);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    void denyMergeRequest(String draftId) {
        OrganizationMergeDraft draft = mergeDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        draft.setStatus(EApprovalStatus.DENIED.getId());
        draft.setApprovedBy(userDetails.getId());
        mergeDraftClient.save(draft);
    }

    UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
