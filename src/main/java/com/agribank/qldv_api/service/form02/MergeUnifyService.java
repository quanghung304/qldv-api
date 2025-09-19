package com.agribank.qldv_api.service.form02;


import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
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
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.entity.DvOrgHistoryDraft;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.dv_org_history.DvOrganizationHisRequest;
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

import java.util.*;
import java.util.stream.Collectors;

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
    DvOrgHistoryDraftClient dvOrgHistoryDraftClient;
    OrganizationMergeDetailClient organizationMergeDetailClient;
    OrganizationHistoryClient organizationHistoryClient;

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

    public List<DV> createDvOrgHistory(String draftId, OrganizationMerge organizationMerge,  List<DvOrgHistory> dvOrgHistories, int type){
        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(draftId).getData();
        if (dvOrgHistoryDrafts.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> staffCodes = new ArrayList<>();
        List<String> organizationCodes = new ArrayList<>();
        for (DvOrgHistoryDraft dvOrgHistoryDraft : dvOrgHistoryDrafts) {
            DvOrgHistory dvOrgHistory = DvOrgHistory.builder()
                    .oldOrgCode(dvOrgHistoryDraft.getOldOrgCode())
                    .newOrgCode(dvOrgHistoryDraft.getNewOrgCode())
                    .staffCode(dvOrgHistoryDraft.getStaffCode())
                    .action(String.valueOf(type))
                    .refId(organizationMerge.getId())
                    .effectiveDate(organizationMerge.getEffectiveDate())
                    .build();

            dvOrgHistories.add(dvOrgHistory);
            staffCodes.add(dvOrgHistoryDraft.getStaffCode());
            organizationCodes.add(dvOrgHistoryDraft.getNewOrgCode());
        }

        List<DV> unifiedMembers = dvClient.findByStaffCodes(staffCodes).getData();

        for (DV member : unifiedMembers) {
            member.setOrganizationCode(organizationMerge.getOrganizationCode());
        }

        List<DvOrgHistory> dvOrgHistoryListInDb = dvOrgService.getOrgHis(DvOrganizationHisRequest.builder()
                .refId(organizationMerge.getId())
                .newOrgCodes(organizationCodes)
                .action(String.valueOf(type))
                .build());

        if (!dvOrgHistoryListInDb.isEmpty()) {
            Map<String, DvOrgHistory> dvOrgHistoriesMap = new HashMap<>();

            for (DvOrgHistory dvOrgHistory : dvOrgHistoryListInDb) {
                dvOrgHistoriesMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }

            for (DvOrgHistory dvOrgHistory : dvOrgHistories) {
                DvOrgHistory dvOrgHis = dvOrgHistoriesMap.getOrDefault(dvOrgHistory.getStaffCode(), null);
                if (Objects.nonNull(dvOrgHis)) {
                    dvOrgHistory = dvOrgHis;
                }
                dvOrgHistory.setEffectiveDate(organizationMerge.getEffectiveDate());
            }
        }

        return unifiedMembers;
    }

    public List<DV> rollBackDV(String refId, OrganizationMerge organizationMerge, List<DvOrgHistory> oldDvOrgHistories){
        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(refId).getData();
        if (dvOrgHistoryDrafts.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, DvOrgHistoryDraft> dvOrgHistoryMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistoryDraft dvOrgHistory : dvOrgHistoryDrafts) {
            dvOrgHistoryMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
        }

        Map<String, DvOrgHistory> dvOrgHistoryRollbackMap = new HashMap<>();
        for (DvOrgHistory dvOrgHistory : oldDvOrgHistories) {
            DvOrgHistoryDraft dvOrgHistoryDraft = dvOrgHistoryMap.getOrDefault(dvOrgHistory.getStaffCode(), null);
            if (Objects.isNull(dvOrgHistoryDraft)) {
                staffCodes.add(dvOrgHistory.getStaffCode());
                dvOrgHistoryRollbackMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }
        }

        List<DV> oldDVs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : oldDVs) {
            if (Objects.equals(dv.getOrganizationCode(), organizationMerge.getOrganizationCode())) {
                String oldCode = dvOrgHistoryRollbackMap.get(dv.getStaffCode()).getOldOrgCode();
                dv.setOrganizationCode(oldCode);
            }
        }

        return oldDVs;
    }

    public OrganizationMergeResponse getDetail(String id) {
        OrganizationMerge organizationMerge = mergeClient.findById(id).getData().orElse(null);

        if (Objects.isNull(organizationMerge)) {
            throw new CommonException("Không tìm thấy yêu cầu sáp nhập");
        }

        OrganizationMergeResponse response = modelMapper.map(organizationMerge, OrganizationMergeResponse.class);
        Organization mergeOrganization = organizationClient.findByCode(organizationMerge.getOrganizationCode()).getData();
        response.setOrganizationMerge(mergeOrganization);

        List<OrganizationMergeDetail> mergeDetails = mergeDetailClient.findByRefId(id).getData();

        List<String> organizations = new ArrayList<>();
        for (OrganizationMergeDetail mergeDetail: mergeDetails) {
            organizations.add(mergeDetail.getOldCode());
        }

        List<Organization> oldOrganizations = organizationClient.findAllByCode(organizations).getData();

        Map<String, Organization> oldOrganizationMaps = new HashMap<>();

        for (Organization organization : oldOrganizations) {
            oldOrganizationMaps.put(organization.getCode(), organization);
        }

        List<DvOrgHistory> dvOrgHistories = dvOrgHistoryClient.findByRefId(id).getData();

        Map<String, List<DvOrgHistory>> dvOrgHistoryMap = dvOrgHistories.stream()
                .collect(Collectors.groupingBy(DvOrgHistory::getOldOrgCode));

//        if (dvOrgHistoryMap.isEmpty()) {
//            throw new CommonException("Không có dữ liệu đảng bộ sáp nhập");
//        }

        Map<String, DV> dvOfOldOrgMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistory dvOrgHistory : dvOrgHistories) {
            staffCodes.add(dvOrgHistory.getStaffCode());
        }
        List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : dvs) {
            dvOfOldOrgMap.put(dv.getStaffCode(), dv);
        }

        List<OrganizationMergeResponse.MergeDetailResponse> detailResponses = new ArrayList<>();

        for (OrganizationMergeDetail mergeDetail: mergeDetails) {
            OrganizationMergeResponse.MergeDetailResponse detailResponse = new OrganizationMergeResponse.MergeDetailResponse();
            List<DvOrgHistory> items = dvOrgHistoryMap.get(mergeDetail.getOldCode());
            List<DV> dvsOfOldOrg = new ArrayList<>();

            if (!Objects.isNull(items)) {
                for (DvOrgHistory dvOrgHistory : items) {
                    dvsOfOldOrg.add(dvOfOldOrgMap.get(dvOrgHistory.getStaffCode()));
                }
            }

            detailResponse.setOrganization(oldOrganizationMaps.get(mergeDetail.getOldCode()));
            detailResponse.setMembers(dvsOfOldOrg);
            detailResponses.add(detailResponse);
        }

        response.setMergeDetails(detailResponses);
        return response;
    }
}
