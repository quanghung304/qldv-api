package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDetailClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.SplitOrganizationRequest;
import com.agribank.qldv_api.request.form02.SplitOrganizationUpdateRequest;
import com.agribank.qldv_api.response.form02.OrganizationSplitDetailResponse;
import com.agribank.qldv_api.response.form02.OrganizationSplitResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.BaseFormEntity;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplit;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetail;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetailDraft;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.SearchOrganizationSplitRequest;
import com.agribank.qldvutils.response.PageResponse;
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
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class SplitOrganizationService implements EntityHandler {
    final ModelMapper modelMapper;
    final ObjectMapper objectMapper;

    final RequestClient requestClient;
    final OrganizationClient organizationClient;
    final OrganizationSplitClient organizationSplitClient;
    final OrganizationSplitDraftClient splitDraftClient;
    final OrganizationSplitDetailDraftClient splitDetailDraftClient;
    final OrganizationSplitDetailClient splitDetailClient;
    OrganizationService organizationService;

    final RequestService requestService;

    static final EForm form = EForm.BIEU_02_SPLIT;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new HashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationSplit.FIELD_MAP);

        return combinedFieldMap;
    }

    public String createSplitRequest(SplitOrganizationRequest request) {
        OrganizationSplitDraft existSplitDraft = splitDraftClient.findPendingRequest(request.getOldCode()).getData();

        if (Objects.nonNull(existSplitDraft)) {
            throw new CommonException("Yêu cầu chia tách tổ chức đảng đã tồn tại");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Organization oldOrganization = organizationClient.findByCode(request.getOldCode()).getData();

        if (Objects.isNull(oldOrganization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        OrganizationSplitDraft splitDraft = modelMapper.map(request, OrganizationSplitDraft.class);
        splitDraft.setOldName(oldOrganization.getName());
        splitDraft.setCreatedBy(userDetails.getId());
        splitDraft = splitDraftClient.save(splitDraft).getData();

        List<OrganizationSplitDetailDraft> detailDraftList = new ArrayList<>();
        for (SplitOrganizationRequest.SplitDetailRequest detailRequest : request.getDetailRequests()) {
            OrganizationSplitDetailDraft detailDraft = modelMapper.map(detailRequest, OrganizationSplitDetailDraft.class);
            detailDraft.setSplitId(splitDraft.getId());
            detailDraftList.add(detailDraft);
        }
        splitDetailDraftClient.saveAll(detailDraftList);

        Request splitRequest = requestService.initializeRequest(splitDraft, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(splitDraft, detailDraftList);
        splitRequest.setNewData(jsonData);
        splitRequest.setOrganizationCode(request.getOldCode());
        splitRequest.setReferenceId(splitDraft.getId());
        splitRequest.setCreatedBy(userDetails.getId());
        requestClient.save(splitRequest);

        return "Tạo mới yêu cầu chia tách tổ chức đảng thành công";
    }

    private String createJsonData(Object splitDraft, List<?> detailDraftList) {
        try {
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(splitDraft, getCombinedFieldMap());
            Map<String, String> fieldMap = OrganizationSplitDetailDraft.FIELD_MAP;
            int i = 1;

            for (Object detailDraft : detailDraftList) {
                BeanWrapper wrapper = new BeanWrapperImpl(detailDraft);

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

    private String getParentCode(String code) {
        if (!Constants.ORGANIZATION_NAME_LENGTH.contains(code.length())) {
            throw new CommonException("Mã tổ chức đảng không hợp lệ");
        }

        if (code.length() == Constants.ORGANIZATION_NAME_LENGTH.get(0)) {
            return Constants.DANG_UY_AGRIBANK_CODE;
        }

        return code.substring(0, code.length() - 2);
    }

    public RequestResponse update(SplitOrganizationUpdateRequest request) {
        OrganizationSplit updateSplitOrganization = organizationSplitClient.findById(request.getId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu. Vui lòng kiểm tra lại"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationSplitDraft splitDraft = modelMapper.map(request, OrganizationSplitDraft.class);

        splitDraft.setRefId(updateSplitOrganization.getId());
        splitDraft.setOldName(updateSplitOrganization.getOldName());
        splitDraft.setCreatedBy(userDetails.getId());
        splitDraft = splitDraftClient.save(splitDraft).getData();

        List<OrganizationSplitDetailDraft> detailDraftList = new ArrayList<>();
        for (SplitOrganizationRequest.SplitDetailRequest detailRequest : request.getDetailRequests()) {
            OrganizationSplitDetailDraft detailDraft = modelMapper.map(detailRequest, OrganizationSplitDetailDraft.class);
            detailDraft.setSplitId(splitDraft.getId());
            detailDraftList.add(detailDraft);
        }
        splitDetailDraftClient.saveAll(detailDraftList);

        Request splitRequest = requestService.initializeRequest(splitDraft, updateSplitOrganization, form, getCombinedFieldMap());

        List<OrganizationSplitDetail> oldDetails = splitDetailClient.findBySplitId(updateSplitOrganization.getId()).getData();
        String newJsonData = createJsonData(splitDraft, detailDraftList);
        String oldJsonData = createJsonData(updateSplitOrganization, oldDetails);
        splitRequest.setNewData(newJsonData);
        splitRequest.setOldData(oldJsonData);
        splitRequest.setOrganizationCode(request.getOldCode());
        splitRequest.setReferenceId(splitDraft.getId());
        splitRequest.setCreatedBy(userDetails.getId());
        requestClient.save(splitRequest);

        return modelMapper.map(splitRequest, RequestResponse.class);
    }

    @Override
    @Transactional
    public boolean applyCreate(String splitDraftId, UserDetailsImpl userDetails) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(splitDraftId).getData().orElse(null);

        if (Objects.isNull(splitDraft) || !Objects.equals(splitDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Yêu cầu không hợp lệ");
        }

        List<OrganizationSplitDetailDraft> detailDrafts = splitDetailDraftClient.findBySplitId(splitDraftId).getData();

        //Luu ho so ban goc
        OrganizationSplit organizationSplit = modelMapper.map(splitDraft, OrganizationSplit.class);
        organizationSplit = organizationSplitClient.save(organizationSplit).getData();

        List<Organization> newOrganizationList = new ArrayList<>();

        //luu danh sach to chuc Dang sau chia tach
        List<OrganizationSplitDetail> details = new ArrayList<>();

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            OrganizationSplitDetail detail = modelMapper.map(detailDraft, OrganizationSplitDetail.class);
            detail.setSplitId(organizationSplit.getId());
            details.add(detail);
        }
        splitDetailClient.saveAll(details);

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            String code = detailDraft.getNewCode();

            Organization organization = Organization.builder()
                    .name(detailDraft.getNewName())
                    .form(detailDraft.getForm())
                    .status(EOrganizationStatus.YES.getStatus())
                    .build();

            organization.setCode(code);
            organization.setParentCode(getParentCode(code));

            newOrganizationList.add(organization);
        }

        Organization oldOrganization = organizationClient.findByCode(splitDraft.getOldCode()).getData();
        oldOrganization.setStatus(EOrganizationStatus.NO.getStatus());
        organizationClient.save(oldOrganization);//set trang thai khong hoat dong cho To chuc Dang bi chia tach
        organizationClient.saveAll(newOrganizationList);

        //luu trang thai draft
        splitDraft.setApprovedBy(userDetails.getId());
        splitDraft.setStatus(EApprovalStatus.APPROVED.getId());
        splitDraftClient.save(splitDraft);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        OrganizationSplitDraft organizationSplitDraft = splitDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationSplitDetailDraft> detailDrafts = splitDetailDraftClient.findBySplitId(organizationSplitDraft.getId()).getData();
        if (detailDrafts.isEmpty()) {
            return false;
        }

        OrganizationSplit organizationSplit = organizationSplitClient.findById(organizationSplitDraft.getRefId())
                .getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationSplitDetail> organizationSplitDetails = splitDetailClient.findBySplitId(organizationSplit.getId()).getData();
        if (organizationSplitDetails.isEmpty()) {
            return false;
        }

        organizationSplit.setEffectiveDate(organizationSplitDraft.getEffectiveDate());
        organizationSplit.setDecisionCommittee(organizationSplitDraft.getDecisionCommittee());
        organizationSplit.setDecisionDate(organizationSplitDraft.getDecisionDate());
        organizationSplit.setDecisionNumber(organizationSplitDraft.getDecisionNumber());
        organizationSplit.setConclusionDate(organizationSplitDraft.getConclusionDate());
        organizationSplit.setConclusionNumber(organizationSplitDraft.getConclusionNumber());


        //biến lấy danh sách để xóa bảng OrganizationSplitDetail đi
        List<OrganizationSplitDetail> removeList = new ArrayList<>();
        //Lấy danh sách OrganizationSplitDetail mới
        List<OrganizationSplitDetail> newDetailList = new ArrayList<>();
        //biến lấy danh sách để xóa cac to chuc cu trong bảng Organization
        List<String> idsOldOrganization = new ArrayList<>();
        //Lấy danh sách them cac to chuc moi trong bảng Organization
        List<Organization> newOrganizations = new ArrayList<>();

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            OrganizationSplitDetail organizationSplitDetail = modelMapper.map(detailDraft, OrganizationSplitDetail.class);
            organizationSplitDetail.setSplitId(organizationSplit.getId());
            newDetailList.add(organizationSplitDetail);
        }

        for (OrganizationSplitDetail detail : organizationSplitDetails) {
            removeList.add(detail);
            idsOldOrganization.add(detail.getNewCode());
        }
        organizationClient.deleteAllById(idsOldOrganization);
        splitDetailClient.deleteAll(removeList);

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            Organization organization = new Organization();
            organization.setCode(detailDraft.getNewCode());
            organization.setName(detailDraft.getNewName());
            organization.setForm(detailDraft.getForm());
            organization.setParentCode(getParentCode(detailDraft.getNewCode()));
            organization.setStatus(EOrganizationStatus.YES.getStatus());

            newOrganizations.add(organization);
        }
        organizationClient.saveAll(newOrganizations);

        saveUpdate(organizationSplitDraft, organizationSplit, newDetailList);
        return true;
    }

    private void saveUpdate(OrganizationSplitDraft organizationSplitDraft,
                            OrganizationSplit organizationSplit,
                            List<OrganizationSplitDetail> newDetailList) {
        organizationSplitDraft.setApprovedBy(getUserRequested().getId());
        organizationSplitDraft.setStatus(EApprovalStatus.APPROVED.getId());
        splitDraftClient.save(organizationSplitDraft);
        splitDetailClient.saveAll(newDetailList);
        organizationSplitClient.save(organizationSplit);
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String splitDraftId) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(splitDraftId).getData().orElse(null);
        if (Objects.nonNull(splitDraft)) {
            splitDraft.setStatus(EApprovalStatus.DENIED.getId());
        }
        splitDraftClient.save(splitDraft);
    }

    public PageResponse<OrganizationSplitResponse> search(SearchOrganizationSplitRequest request) {
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<OrganizationSplit> organizationSplitPageResponse = organizationSplitClient.search(request).getData();

        PageResponse<OrganizationSplitResponse> response = new PageResponse<>();
        if (Objects.isNull(organizationSplitPageResponse)) {
            return response;
        }

        response.setCurrentPage(organizationSplitPageResponse.getCurrentPage());
        response.setTotalPages(organizationSplitPageResponse.getTotalPages());
        response.setTotalItems(organizationSplitPageResponse.getTotalItems());
        response.setData(organizationSplitPageResponse.getData()
                .stream()
                .map(o -> modelMapper.map(o, OrganizationSplitResponse.class)).toList());

        return response;
    }

    public OrganizationSplitDetailResponse getDetail(String id) {
        OrganizationSplit organizationSplit = organizationSplitClient.findById(id).getData().orElse(null);

        if (Objects.isNull(organizationSplit)) {
            throw new CommonException("Không tìm thấy yêu cầu hợp nhất");
        }

        OrganizationSplitDetailResponse response = modelMapper.map(organizationSplit, OrganizationSplitDetailResponse.class);

        List<OrganizationSplitDetail> details = splitDetailClient.findBySplitId(id).getData();
        List<OrganizationSplitDetailResponse.NewOrganizationResponse> detailResponses = new ArrayList<>();

        for (OrganizationSplitDetail detail : details) {
            OrganizationSplitDetailResponse.NewOrganizationResponse detailResponse = modelMapper.map(detail, OrganizationSplitDetailResponse.NewOrganizationResponse.class);
            detailResponses.add(detailResponse);
        }

        response.setSplitDetails(detailResponses);
        return response;
    }

    private UserDetailsImpl getUserRequested() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
