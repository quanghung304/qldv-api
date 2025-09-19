package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDetailClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.split.OrganizationSplitDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.SplitDraftTempRequest;
import com.agribank.qldv_api.request.form02.SplitOrganizationRequest;
import com.agribank.qldv_api.request.form02.SplitOrganizationUpdateRequest;
import com.agribank.qldv_api.response.form02.OrganizationSplitDetailResponse;
import com.agribank.qldv_api.response.form02.OrganizationSplitResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplit;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetail;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetailDraft;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.*;
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
import java.util.stream.Collectors;

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
    final DVClient dvClient;
    final DvOrgHistoryDraftClient dvOrgHistoryDraftClient;
    final DvOrgHistoryClient dvOrgHistoryClient;
    final OrganizationHistoryClient organizationHistoryClient;
    final RequestService requestService;
    OrganizationService organizationService;

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

        SplitDraftTempRequest draftRequest = new SplitDraftTempRequest();
        List<OrganizationSplitDetailDraft> detailDraftList = createSplitDetailAndDvOrgHistoryDrafts(request, splitDraft, draftRequest);

        Request splitRequest = requestService.initializeRequest(splitDraft, null, form, getCombinedFieldMap());
        String jsonData = createJsonData(splitDraft, detailDraftList);
        splitRequest.setNewData(jsonData);
        splitRequest.setOrganizationCode(request.getOldCode());
        splitRequest.setReferenceId(splitDraft.getId());
        splitRequest.setCreatedBy(userDetails.getId());

        SplitDraftRequest splitDraftRequest = SplitDraftRequest.builder()
                .splitDetailDrafts(draftRequest.getSplitDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .splitRequest(splitRequest)
                .build();

        organizationSplitClient.saveDraftEntities(splitDraftRequest);

        return "Tạo mới yêu cầu chia tách tổ chức đảng thành công";
    }

    private List<OrganizationSplitDetailDraft> createSplitDetailAndDvOrgHistoryDrafts(SplitOrganizationRequest request, OrganizationSplitDraft splitDraft, SplitDraftTempRequest draftRequest){
        if (request.getDetailRequestsNew().isEmpty()){
            return new ArrayList<>();
        }

        List<OrganizationSplitDetailDraft> detailDraftList = new ArrayList<>();
        List<DvOrgHistoryDraft> membersDraft = new ArrayList<>();

        for (SplitOrganizationRequest.SplitDetailRequestNew detailRequest : request.getDetailRequestsNew()) {
            OrganizationSplitDetailDraft detailDraft = modelMapper.map(detailRequest, OrganizationSplitDetailDraft.class);
            detailDraft.setSplitId(splitDraft.getId());
            detailDraftList.add(detailDraft);

            List<DV> DVs = dvClient.findByStaffCodes(detailRequest.getMembers()).getData();

            for (DV dv : DVs) {
                DvOrgHistoryDraft memDraft = new DvOrgHistoryDraft();
                memDraft.setStaffCode(dv.getStaffCode());
                memDraft.setOldOrgCode(dv.getOrganizationCode());
                memDraft.setNewOrgCode(detailRequest.getNewCode());
                memDraft.setRefId(splitDraft.getId());
                membersDraft.add(memDraft);
            }
        }

        for (SplitOrganizationRequest.SplitDetailRequestOld detailRequest : request.getDetailRequestsOld()) {
            OrganizationSplitDetailDraft detailDraft = new OrganizationSplitDetailDraft();
            detailDraft.setNewCode(detailRequest.getOrganizationCode());
            detailDraft.setSplitId(splitDraft.getId());
            detailDraftList.add(detailDraft);

            List<DV> DVs = dvClient.findByStaffCodes(detailRequest.getMembers()).getData();

            for (DV dv : DVs) {
                DvOrgHistoryDraft memDraft = new DvOrgHistoryDraft();
                memDraft.setStaffCode(dv.getStaffCode());
                memDraft.setOldOrgCode(dv.getOrganizationCode());
                memDraft.setNewOrgCode(detailRequest.getOrganizationCode());
                memDraft.setRefId(splitDraft.getId());
                membersDraft.add(memDraft);
            }
        }

        draftRequest.setMembersDraft(membersDraft);
        draftRequest.setSplitDetailDrafts(detailDraftList);

        return detailDraftList;
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

        SplitDraftTempRequest draftRequest = new SplitDraftTempRequest();
        List<OrganizationSplitDetailDraft> detailDraftList = createSplitDetailAndDvOrgHistoryDrafts(request, splitDraft, draftRequest);

        Request splitRequest = requestService.initializeRequest(splitDraft, updateSplitOrganization, form, getCombinedFieldMap());

        List<OrganizationSplitDetail> oldDetails = splitDetailClient.findBySplitId(updateSplitOrganization.getId()).getData();
        String newJsonData = createJsonData(splitDraft, detailDraftList);
        String oldJsonData = createJsonData(updateSplitOrganization, oldDetails);
        splitRequest.setNewData(newJsonData);
        splitRequest.setOldData(oldJsonData);
        splitRequest.setOrganizationCode(request.getOldCode());
        splitRequest.setReferenceId(splitDraft.getId());
        splitRequest.setCreatedBy(userDetails.getId());

        SplitDraftRequest splitDraftRequest = SplitDraftRequest.builder()
                .splitDetailDrafts(draftRequest.getSplitDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .splitRequest(splitRequest)
                .build();

        organizationSplitClient.saveDraftEntities(splitDraftRequest);

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
        String organizationSplitId = organizationSplit.getId();
        Date organizationSplitEffectiveDate = organizationSplit.getEffectiveDate();

        //luu danh sach to chuc Dang sau chia tach
        List<Organization> newOrganizationList = new ArrayList<>();
        List<OrganizationSplitDetail> details = new ArrayList<>();

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            OrganizationSplitDetail detail = modelMapper.map(detailDraft, OrganizationSplitDetail.class);
            detail.setSplitId(organizationSplitId);
            details.add(detail);
        }

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            if (detailDraft.getNewName() != null && detailDraft.getForm() != null) {
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
        }

        //set trang thai khong hoat dong cho To chuc Dang bi chia tach va cac To chuc con
        List<Organization> oldOrganizations = organizationClient.findByParent(splitDraft.getOldCode()).getData();

        for (Organization oldOrganization : oldOrganizations) {
            oldOrganization.setStatus(EOrganizationStatus.NO.getStatus());
        }

        //luu trang thai draft
        splitDraft.setApprovedBy(userDetails.getId());
        splitDraft.setStatus(EApprovalStatus.APPROVED.getId());

        //luu chinh thuc danh sach dang vien voi cac to chuc dang moi
        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(splitDraft.getId()).getData();
        List<String> staffCodes = new ArrayList<>();
        Map<String, String> dvOrgHistoriesMap = new HashMap<>();

        List<DvOrgHistory> dvOrgHistories = dvOrgHistoryDrafts.stream().map(dv -> {
            DvOrgHistory dvOrgHistory = modelMapper.map(dv, DvOrgHistory.class);
            dvOrgHistory.setRefId(organizationSplitId);
            dvOrgHistory.setEffectiveDate(organizationSplitEffectiveDate);
            staffCodes.add(dvOrgHistory.getStaffCode());
            dvOrgHistoriesMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory.getNewOrgCode());
            return dvOrgHistory;
        }).toList();

        //cap nhat lai truong organizationCode cua cac Dang vien sau chia tach
        List<DV> newDVs = new ArrayList<>();
        List<DV> DVs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : DVs) {
            String newOrg = dvOrgHistoriesMap.getOrDefault(dv.getStaffCode(), null);
            dv.setOrganizationCode(newOrg);
            newDVs.add(dv);
        }

        List<OrganizationHistory> organizationHistories = new ArrayList<>();

        OrganizationHistory organizationHistory = new OrganizationHistory();
        organizationHistory.setCode(organizationSplit.getOldCode());
        organizationHistory.setName(organizationSplit.getOldName());
        organizationHistory.setRefId(organizationSplit.getId());
        organizationHistory.setEffectiveDate(organizationSplit.getEffectiveDate());
        organizationHistory.setType(EReport01Type.DECOMPOSE.getId());
        organizationHistories.add(organizationHistory);

        for (OrganizationSplitDetail organizationSplitDetail : details) {
            OrganizationHistory organizationHistorySplited = new OrganizationHistory();
            organizationHistorySplited.setCode(organizationSplitDetail.getNewCode());
            organizationHistorySplited.setName(organizationSplitDetail.getNewName());
            organizationHistorySplited.setRefId(organizationSplitDetail.getId());
            organizationHistorySplited.setEffectiveDate(organizationSplit.getEffectiveDate());
            organizationHistorySplited.setType(EReport01Type.DECOMPOSE.getId());
            organizationHistories.add(organizationHistorySplited);
        }

        ApproveSplitRequest request = ApproveSplitRequest.builder()
                .organizationSplitDetails(details)
                .newOrganizations(newOrganizationList)
                .oldOrganizations(oldOrganizations)
                .splitDraft(splitDraft)
                .dvOrgHistories(dvOrgHistories)
                .newDVs(newDVs)
                .organizationHistories(organizationHistories)
                .build();

        organizationSplitClient.saveEntities(request);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        OrganizationSplitDraft organizationSplitDraft = splitDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        organizationSplitDraft.setApprovedBy(getUserRequested().getId());
        organizationSplitDraft.setStatus(EApprovalStatus.APPROVED.getId());

        List<OrganizationSplitDetailDraft> detailDrafts = splitDetailDraftClient.findBySplitId(organizationSplitDraft.getId()).getData();
        if (detailDrafts.isEmpty()) {
            return false;
        }

        OrganizationSplit organizationSplit = organizationSplitClient.findById(organizationSplitDraft.getRefId())
                .getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        List<OrganizationSplitDetail> oldOrganizationSplitDetails = splitDetailClient.findBySplitId(organizationSplit.getId()).getData();
        if (oldOrganizationSplitDetails.isEmpty()) {
            return false;
        }

        organizationSplit.setEffectiveDate(organizationSplitDraft.getEffectiveDate());
        organizationSplit.setDecisionCommittee(organizationSplitDraft.getDecisionCommittee());
        organizationSplit.setDecisionDate(organizationSplitDraft.getDecisionDate());
        organizationSplit.setDecisionNumber(organizationSplitDraft.getDecisionNumber());
        organizationSplit.setConclusionDate(organizationSplitDraft.getConclusionDate());
        organizationSplit.setConclusionNumber(organizationSplitDraft.getConclusionNumber());

        //Lấy danh sách OrganizationSplitDetail mới
        List<OrganizationSplitDetail> newDetailList = new ArrayList<>();
        //biến lấy danh sách để xóa cac to chuc cu trong bảng Organization
        List<String> idOldOrganizations = new ArrayList<>();
        //Lấy danh sách them cac to chuc moi trong bảng Organization
        List<Organization> newOrganizations = new ArrayList<>();

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            OrganizationSplitDetail organizationSplitDetail = modelMapper.map(detailDraft, OrganizationSplitDetail.class);
            organizationSplitDetail.setSplitId(organizationSplit.getId());
            newDetailList.add(organizationSplitDetail);
        }

        for (OrganizationSplitDetail detail : oldOrganizationSplitDetails) {
            if (detail.getNewName() != null && detail.getForm() != null) {
                idOldOrganizations.add(detail.getNewCode());
            }
        }

        for (OrganizationSplitDetailDraft detailDraft : detailDrafts) {
            if (detailDraft.getNewName() != null && detailDraft.getForm() != null) {
                Organization organization = new Organization();
                organization.setCode(detailDraft.getNewCode());
                organization.setName(detailDraft.getNewName());
                organization.setForm(detailDraft.getForm());
                organization.setParentCode(getParentCode(detailDraft.getNewCode()));
                organization.setStatus(EOrganizationStatus.YES.getStatus());

                newOrganizations.add(organization);
            }
        }

        //xoa cac ban ghi luu danh sach dang vien o to chuc cu
        List<DvOrgHistory> oldDvOrgHistories = dvOrgHistoryClient.findByRefId(organizationSplit.getId()).getData();

        //luc chinh thuc danh sach dang vien voi cac to chuc dang moi
        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(organizationSplitDraft.getId()).getData();
        List<String> staffCodes = new ArrayList<>();
        Map<String, String> dvOrgHistoriesMap = new HashMap<>();

        List<DvOrgHistory> newDvOrgHistories = dvOrgHistoryDrafts.stream().map(dv -> {
            DvOrgHistory dvOrgHistory = modelMapper.map(dv, DvOrgHistory.class);
            dvOrgHistory.setRefId(organizationSplit.getId());
            dvOrgHistory.setEffectiveDate(organizationSplit.getEffectiveDate());
            staffCodes.add(dvOrgHistory.getStaffCode());
            dvOrgHistoriesMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory.getNewOrgCode());
            return dvOrgHistory;
        }).toList();

        //cap nhat lai truong organizationCode cua cac Dang vien sau chia tach
        List<DV> newDVs = new ArrayList<>();
        List<DV> DVs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : DVs) {
            String newOrg = dvOrgHistoriesMap.getOrDefault(dv.getStaffCode(), null);
            dv.setOrganizationCode(newOrg);
            newDVs.add(dv);
        }

        List<OrganizationHistory> newOrganizationHistories = new ArrayList<>();
        OrganizationHistory organizationHistory = organizationHistoryClient.findByRefId(EReport01Type.DECOMPOSE.getId(), organizationSplit.getId()).getData();
        organizationHistory.setEffectiveDate(organizationSplitDraft.getEffectiveDate());
        newOrganizationHistories.add(organizationHistory);

        for (OrganizationSplitDetail organizationSplitDetail : newDetailList) {
            OrganizationHistory organizationHistorySplited = new OrganizationHistory();
            organizationHistorySplited.setCode(organizationSplitDetail.getNewCode());
            organizationHistorySplited.setName(organizationSplitDetail.getNewName());
            organizationHistorySplited.setRefId(organizationSplitDetail.getId());
            organizationHistorySplited.setEffectiveDate(organizationSplit.getEffectiveDate());
            organizationHistorySplited.setType(EReport01Type.DECOMPOSE.getId());
            newOrganizationHistories.add(organizationHistorySplited);
        }

        List<OrganizationHistory> oldOrganizationHistories = new ArrayList<>();

        for (OrganizationSplitDetail organizationSplitDetail : oldOrganizationSplitDetails) {
            OrganizationHistory oldOrganizationHistory = organizationHistoryClient.findByRefId(EReport01Type.DECOMPOSE.getId(), organizationSplitDetail.getId()).getData();
            oldOrganizationHistories.add(oldOrganizationHistory);
        }

        ApproveUpdateSplitRequest request = ApproveUpdateSplitRequest.builder()
                .newOrganizationSplitDetails(newDetailList)
                .oldOrganizationSplitDetails(oldOrganizationSplitDetails)
                .newOrganizations(newOrganizations)
                .idOldOrganizations(idOldOrganizations)
                .organizationSplit(organizationSplit)
                .splitDraft(organizationSplitDraft)
                .oldDvOrgHistories(oldDvOrgHistories)
                .newDvOrgHistories(newDvOrgHistories)
                .newDVs(newDVs)
                .newOrganizationHistories(newOrganizationHistories)
                .oldOrganizationHistories(oldOrganizationHistories)
                .build();

        organizationSplitClient.updateEntities(request);
        return true;
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
            throw new CommonException("Không tìm thấy yêu cầu chia tách");
        }

        OrganizationSplitDetailResponse response = modelMapper.map(organizationSplit, OrganizationSplitDetailResponse.class);

        List<OrganizationSplitDetail> details = splitDetailClient.findBySplitId(id).getData();
        List<DvOrgHistory> dvOrgHistories = dvOrgHistoryClient.findByRefId(id).getData();

        Map<String, List<DvOrgHistory>> dvOrgHistoryMap = dvOrgHistories.stream()
                .collect(Collectors.groupingBy(DvOrgHistory::getNewOrgCode));

//        if (dvOrgHistoryMap.isEmpty()) {
//            throw new CommonException("Không có dữ liệu đảng bộ chia tách");
//        }

        Map<String, DV> dvOfNewOrgMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistory dvOrgHistory : dvOrgHistories) {
            staffCodes.add(dvOrgHistory.getStaffCode());
        }
        List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : dvs) {
            dvOfNewOrgMap.put(dv.getStaffCode(), dv);
        }

        List<OrganizationSplitDetailResponse.NewOrganizationResponse> detailResponsesNew = new ArrayList<>();
        List<OrganizationSplitDetailResponse.OldOrganizationResponse> detailResponsesOld = new ArrayList<>();

        for (OrganizationSplitDetail detail : details) {
            if (detail.getNewName() != null && detail.getForm() != null) {
                OrganizationSplitDetailResponse.NewOrganizationResponse detailResponse = modelMapper.map(detail, OrganizationSplitDetailResponse.NewOrganizationResponse.class);
                List<DvOrgHistory> items = dvOrgHistoryMap.get(detailResponse.getCode());
                List<DV> dvsOfNewOrg = new ArrayList<>();

                if (!Objects.isNull(items)) {
                    for (DvOrgHistory dvOrgHistory : items) {
                        dvsOfNewOrg.add(dvOfNewOrgMap.get(dvOrgHistory.getStaffCode()));
                    }
                }

                detailResponse.setMembers(dvsOfNewOrg);
                detailResponsesNew.add(detailResponse);
            }
            else{
                OrganizationSplitDetailResponse.OldOrganizationResponse detailResponse = new OrganizationSplitDetailResponse.OldOrganizationResponse();
                Organization oldOrganization = organizationClient.findByCode(detail.getNewCode()).getData();
                List<DvOrgHistory> items = dvOrgHistoryMap.get(detail.getNewCode());
                List<DV> dvsOfNewOrg = new ArrayList<>();

                if (!Objects.isNull(items)) {
                    for (DvOrgHistory dvOrgHistory : items) {
                        dvsOfNewOrg.add(dvOfNewOrgMap.get(dvOrgHistory.getStaffCode()));
                    }
                }

                detailResponse.setOldOrganization(oldOrganization);
                detailResponse.setMembers(dvsOfNewOrg);
                detailResponsesOld.add(detailResponse);
            }

        }

        response.setSplitDetailsNew(detailResponsesNew);
        response.setSplitDetailsOld(detailResponsesOld);
        return response;
    }

    private UserDetailsImpl getUserRequested() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public OrganizationSplitDetailResponse getDraftDetail(String splitDraftId) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(splitDraftId).getData().orElse(null);

        if (Objects.isNull(splitDraft)) {
            throw new CommonException("Không tìm thấy yêu cầu chia tách");
        }

        OrganizationSplitDetailResponse response = modelMapper.map(splitDraft, OrganizationSplitDetailResponse.class);

        List<OrganizationSplitDetailDraft> detailDrafts = splitDetailDraftClient.findBySplitId(splitDraftId).getData();
        List<DvOrgHistoryDraft> dvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(splitDraft.getId()).getData();

        Map<String, List<DvOrgHistoryDraft>> dvOrgHistoryMap = dvOrgHistoryDrafts.stream()
                .collect(Collectors.groupingBy(DvOrgHistoryDraft::getNewOrgCode));

//        if (dvOrgHistoryMap.isEmpty()) {
//            throw new CommonException("Không có dữ liệu đảng bộ chia tách");
//        }

        Map<String, DV> dvOfNewOrgMap = new HashMap<>();
        List<String> staffCodes = new ArrayList<>();

        for (DvOrgHistoryDraft dvOrgHistory : dvOrgHistoryDrafts) {
            staffCodes.add(dvOrgHistory.getStaffCode());
        }
        List<DV> dvs = dvClient.findByStaffCodeActiveIn(staffCodes).getData();

        for (DV dv : dvs) {
            dvOfNewOrgMap.put(dv.getStaffCode(), dv);
        }

        List<OrganizationSplitDetailResponse.NewOrganizationResponse> detailResponsesNew = new ArrayList<>();
        List<OrganizationSplitDetailResponse.OldOrganizationResponse> detailResponsesOld = new ArrayList<>();

        for (OrganizationSplitDetailDraft detail : detailDrafts) {
            if (detail.getNewName() != null && detail.getForm() != null) {
                OrganizationSplitDetailResponse.NewOrganizationResponse detailResponse = modelMapper.map(detail, OrganizationSplitDetailResponse.NewOrganizationResponse.class);
                List<DvOrgHistoryDraft> items = dvOrgHistoryMap.get(detailResponse.getCode());
                List<DV> dvsOfNewOrg = new ArrayList<>();

                if (!Objects.isNull(items)) {
                    for (DvOrgHistoryDraft dvOrgHistory : items) {
                        dvsOfNewOrg.add(dvOfNewOrgMap.get(dvOrgHistory.getStaffCode()));
                    }
                }

                detailResponse.setMembers(dvsOfNewOrg);
                detailResponsesNew.add(detailResponse);
            }
            else{
                OrganizationSplitDetailResponse.OldOrganizationResponse detailResponse = new OrganizationSplitDetailResponse.OldOrganizationResponse();
                Organization oldOrganization = organizationClient.findByCode(detail.getNewCode()).getData();
                List<DvOrgHistoryDraft> items = dvOrgHistoryMap.get(detail.getNewCode());
                List<DV> dvsOfNewOrg = new ArrayList<>();

                if (!Objects.isNull(items)) {
                    for (DvOrgHistoryDraft dvOrgHistory : items) {
                        dvsOfNewOrg.add(dvOfNewOrgMap.get(dvOrgHistory.getStaffCode()));
                    }
                }

                detailResponse.setOldOrganization(oldOrganization);
                detailResponse.setMembers(dvsOfNewOrg);
                detailResponsesOld.add(detailResponse);
            }
        }

        response.setSplitDetailsNew(detailResponsesNew);
        response.setSplitDetailsOld(detailResponsesOld);
        return response;
    }

    public String updateDraft(SplitOrganizationUpdateRequest request) {
        OrganizationSplitDraft splitDraft = splitDraftClient.findById(request.getId()).getData().orElseThrow(() -> new CommonException(ExceptionMessage.NO_DATA));;

        if (!Objects.equals(splitDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException("Chỉ được chỉnh sửa yêu cầu chưa được phê duyệt");
        }

        Organization oldOrganization = organizationClient.findByCode(request.getOldCode()).getData();

        if (Objects.isNull(oldOrganization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        splitDraft.setOldCode(request.getOldCode());
        splitDraft.setOldName(oldOrganization.getName());
        splitDraft.setDecisionDate(request.getDecisionDate());
        splitDraft.setConclusionNumber(request.getConclusionNumber());
        splitDraft.setDecisionNumber(request.getDecisionNumber());
        splitDraft.setConclusionDate(request.getConclusionDate());
        splitDraft.setDecisionCommittee(request.getDecisionCommittee());
        splitDraft.setEffectiveDate(request.getEffectiveDate());

        List<OrganizationSplitDetailDraft> oldOrganizationSplitDetailDrafts = splitDetailDraftClient.findBySplitId(splitDraft.getId()).getData();
        if (oldOrganizationSplitDetailDrafts.isEmpty()) {
            throw new CommonException("Không tìm thấy thông tin tổ chức Đảng sau chia tách");
        }

        List<DvOrgHistoryDraft> oldDvOrgHistoryDrafts = dvOrgHistoryDraftClient.findByRefId(splitDraft.getId()).getData();

        SplitDraftTempRequest draftRequest = new SplitDraftTempRequest();
        List<OrganizationSplitDetailDraft> detailDraftList = createSplitDetailAndDvOrgHistoryDrafts(request, splitDraft, draftRequest);

        Request splitRequest = requestService.getRequestByDraftId(form.getCode(), request.getId());
        String jsonData = createJsonData(splitDraft, detailDraftList);
        splitRequest.setNewData(jsonData);
        splitRequest.setOrganizationCode(request.getOldCode());

        SplitUpdateDraftRequest splitUpdateDraftRequest = SplitUpdateDraftRequest.builder()
                .splitDetailDrafts(draftRequest.getSplitDetailDrafts())
                .membersDraft(draftRequest.getMembersDraft())
                .splitRequest(splitRequest)
                .splitDraft(splitDraft)
                .oldOrganizationSplitDetailDrafts(oldOrganizationSplitDetailDrafts)
                .oldDvOrgHistoryDrafts(oldDvOrgHistoryDrafts)
                .build();

        organizationSplitClient.saveUpdateDraftEntities(splitUpdateDraftRequest);

        return "Cập nhật yêu cầu thành công";
    }

}
