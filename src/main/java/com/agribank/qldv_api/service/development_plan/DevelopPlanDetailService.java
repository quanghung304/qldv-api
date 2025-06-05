package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.ERecordStatus;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.development_plan.DevelopmentPlanDetailClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.develop_plan.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.develop_plan.DevelopPlanDetailResponse;
import com.agribank.qldv_api.response.develop_plan.DevelopPlanResponse;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlan;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetail;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetailDraft;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.development_plan.DevelopDetailRefIdRequest;
import com.agribank.qldvutils.request.development_plan.DevelopPrntBrcdRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DevelopPlanDetailService implements EntityHandler {
    private final RequestClient requestClient;
    private final DevelopmentPlanDetailClient developmentPlanDetailClient;
    private final DevelopPlanService developPlanService;
    private final CheckAuthorityService checkAuthorityService;
    private final ModelMapper modelMapper;
    private final DevelopmentPlanDetailDraftService developPlanDetailDraftService;
    private final DevelopPlanDraftService developPlanDraftService;
    private final RequestService requestService;
    private final EForm form = EForm.BIEU_12;
    private final DevelopmentPlanDetailDraftService developmentPlanDetailDraftService;
    private final OrganizationService organizationService;



    private Map<String, String> getCombinedFieldMap() {
        return new HashMap<>(DevelopmentPlanDetailDraft.BASE_FIELD_MAP);
    }

    public List<DevelopPlanDetailResponse> getPlanDetail(GetDevelopmentPlanRequest request) {
        try {
            PageResponse<DevelopPlanResponse> planPageResponse = developPlanService.getPlan(request);
            if (planPageResponse.getData().isEmpty()) {
                return null;
            }
            DefaultResponse<List<DevelopmentPlanDetail>> response = developmentPlanDetailClient.getByRefId(planPageResponse.getData().get(0).getId());
            if (!response.getSuccess()) {
                return null;
            }
            if (response.getData().isEmpty()) {
                return null;
            }
            return response.getData().stream()
                    .map(role -> modelMapper.map(role, DevelopPlanDetailResponse.class))
                    .toList();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public String addDevelopPlanDetail(DevelopPlanDataRequest dataRequest) {
        checkAuthorityService.hasAuthorityOverOrganization(dataRequest.getOrganizationCode());
        String parentOrganization = getPrntOrganization(dataRequest.getOrganizationCode());
        DevelopmentPlan developmentPlan = developPlanService.searchPrntBrcd(new DevelopPrntBrcdRequest(
                dataRequest.getOrganizationCode(),
                dataRequest.getStart(),
                dataRequest.getEnd()));

        if (Objects.nonNull(developmentPlan)) {
            throw new CommonException("Kế hoạch phát triển đã tồn tại");
        }

        UserDetailsImpl userRequested = getUserRequested();
        DevelopmentPlanDraft developmentPlanDraft = DevelopmentPlanDraft.builder()
                    .organizationCode(dataRequest.getOrganizationCode())
                    .name(dataRequest.getName())
                    .start(dataRequest.getStart())
                    .end(dataRequest.getEnd())
                    .prntCode(parentOrganization)
                    .hasChild(dataRequest.getData().isEmpty() ? 0 : 1)
                    .status(EApprovalStatus.PENDING.getId())
                    .createdBy(userRequested.getUsername())
                    .build();
        developmentPlanDraft = developPlanDraftService.save(developmentPlanDraft);

        if (dataRequest.getData().isEmpty()) {
            return "Thêm kế hoạch phát triển thành công!";
        }
        List<Map<String, Object>> developDetailMap = new ArrayList<>();

        List<DevelopmentPlanDetailDraft> developmentPlanDetailDraftList = new ArrayList<>();
        buildDevelopmentPlanDetailDrafts(developmentPlanDetailDraftList, developDetailMap, dataRequest, developmentPlanDraft);
        developPlanDetailDraftService.saveAll(developmentPlanDetailDraftList);

        int totalTarget = dataRequest.getData().stream().mapToInt(DevelopPlanDetailRequest::getTarget).sum();

        developmentPlanDraft.setTarget(totalTarget);
        developPlanDraftService.save(developmentPlanDraft);

        Request developRequest = requestService.initializeRequest(developmentPlanDraft, null, form, developPlanService.getCombinedFieldMap());
        Map<String, Object> developPlanDraftMap = CommonUtils.createFilteredDataMap(developmentPlanDraft, developPlanService.getCombinedFieldMap());
        String newData = String.format("""
                    {
                        developPlan: %s,
                        developPlanDetail: %s,
                    }
                    """, developPlanDraftMap, developDetailMap);
        developRequest.setNewData(newData);
        developRequest.setCreatedBy(userRequested.getId());
        developRequest.setOrganizationCode(developmentPlanDraft.getOrganizationCode());

        developRequest.setReferenceId(developmentPlanDraft.getId());
        requestClient.save(developRequest);

        return "Thêm kế hoạch phát triển thành công!";
    }

    private void buildDevelopmentPlanDetailDrafts(List<DevelopmentPlanDetailDraft> developPlanDetailDraftList,
                                                  List<Map<String, Object>> mapList,
                                                  DevelopPlanDataRequest dataRequest,
                                                  DevelopmentPlanDraft developmentPlanDraft
                                                ) {
        for (DevelopPlanDetailRequest year : dataRequest.getData()){
            DevelopmentPlanDetailDraft developmentPlanDetailDraft = DevelopmentPlanDetailDraft.builder()
                    .refId(developmentPlanDraft.getId())
                    .target(year.getTarget())
                    .min(year.getMin())
                    .year(year.getYear())
                    .build();
            Map<String, Object> map = CommonUtils.createFilteredDataMap(developmentPlanDetailDraft, getCombinedFieldMap());
            mapList.add(map);
            developPlanDetailDraftList.add(developmentPlanDetailDraft);
        }
    }

    private String getPrntOrganization(String brcd) {
        String prnt = null;
        OrganizationResponse prntBrcds = organizationService.get(brcd);
        if (Objects.nonNull(prntBrcds) ) {
            prnt = prntBrcds.getParentCode();
        }

        if (!Objects.equals(brcd, prnt) && Objects.nonNull(prnt) && !prnt.equals("1000")) {
            return prnt;
        }
        return null;
    }

    public String updateDevelopPlanDetail(DevelopPlanDetailUpdateRequest dataRequest) {
        checkAuthorityService.hasAuthorityOverOrganization(dataRequest.getOrganizationCode());

        DevelopmentPlan developmentPlan = developPlanService.findById(dataRequest.getRefId());
        if (Objects.isNull(developmentPlan)) {
            throw new CommonException("Không tìm thấy kế hoạch phát triển Đảng viên cần chỉnh sửa! Vui lòng kiểm tra lại.");
        }

        List<Integer> years = dataRequest.getData().stream().map(DevelopPlanDetailRequest::getYear).toList();
        DevelopDetailRefIdRequest developDetailRefIdRequest = DevelopDetailRefIdRequest.builder()
                .refId(dataRequest.getRefId())
                .years(years)
                .build();
        List<DevelopmentPlanDetail> developmentPlanDetails = developmentPlanDetailClient
                .findByRefIdAndYearIn(developDetailRefIdRequest).getData();

        if (developmentPlanDetails.isEmpty()) {
            throw new CommonException("Kiểm tra refId và các chỉ tiêu năm");
        }

        DevelopmentPlanDraft developmentPlanDraft = modelMapper.map(developmentPlan, DevelopmentPlanDraft.class);
        developmentPlanDraft.setName(dataRequest.getName());
        developmentPlanDraft.setStart(dataRequest.getStart());
        developmentPlanDraft.setEnd(dataRequest.getEnd());
        developmentPlanDraft.setRefId(developmentPlanDraft.getId());

        UserDetailsImpl userRequested = getUserRequested();
        developmentPlanDraft.setStatus(EApprovalStatus.PENDING.getId());
        developmentPlanDraft.setCreatedBy(userRequested.getId());
        developmentPlanDraft = developPlanDraftService.save(developmentPlanDraft);

        Request developRequest = getRequest(developmentPlan, dataRequest, developmentPlanDetails, developmentPlanDraft, userRequested);
        requestClient.save(developRequest);

        return "Tạo yêu cầu thành công";
    }

    private Request getRequest(
                                  DevelopmentPlan developmentPlan,
                                  DevelopPlanDetailUpdateRequest dataRequest,
                                  List<DevelopmentPlanDetail> developmentPlanDetails,
                                  DevelopmentPlanDraft developmentPlanDraft,
                                  UserDetailsImpl userRequested
                                  ){
        Map<String, Object> oldDevelopPlanMap = CommonUtils.createFilteredDataMap(developmentPlan, developPlanService.getCombinedFieldMap());
        List<Map<String, Object>> oldDevelopPlanDetail = new ArrayList<>();
        for (DevelopmentPlanDetail developmentPlanDetail : developmentPlanDetails) {
            Map<String, Object> map = CommonUtils.createFilteredDataMap(developmentPlanDetail, getCombinedFieldMap());
            oldDevelopPlanDetail.add(map);
        }

        String oldData = String.format("""
                    {
                        developPlan: %s,
                        developPlanDetail: %s,
                    }
                    """, oldDevelopPlanMap, oldDevelopPlanDetail);

        Map<String, Object> newDevelopPlanMap = CommonUtils.createFilteredDataMap(developmentPlanDraft, developPlanService.getCombinedFieldMap());
        List<Map<String, Object>> newDevelopPlanDetail = new ArrayList<>();
        List<DevelopmentPlanDetailDraft> developmentPlanDetailDraftList = new ArrayList<>();
        for (DevelopPlanDetailRequest developmentPlanDetail : dataRequest.getData()) {
            Map<String, Object> map = CommonUtils.createFilteredDataMap(developmentPlanDetail, getCombinedFieldMap());
            newDevelopPlanDetail.add(map);

            DevelopmentPlanDetailDraft developmentPlanDetailDraft = modelMapper.map(developmentPlanDetail, DevelopmentPlanDetailDraft.class);
            developmentPlanDetailDraft.setRefId(developmentPlanDraft.getId());
            developmentPlanDetailDraftList.add(developmentPlanDetailDraft);
        }

        developPlanDetailDraftService.saveAll(developmentPlanDetailDraftList);
        String newData = String.format("""
                    {
                        developPlan: %s,
                        developPlanDetail: %s,
                    }
                    """, newDevelopPlanMap, newDevelopPlanDetail);
        Request developRequest = requestService.initializeRequest(developmentPlanDraft, developmentPlan, form, developPlanService.getCombinedFieldMap());
        developRequest.setNewData(newData);
        developRequest.setOldData(oldData);
        developRequest.setCreatedBy(userRequested.getId());
        developRequest.setOrganizationCode(dataRequest.getOrganizationCode());
        developRequest.setReferenceId(developmentPlanDraft.getId());

        return developRequest;
    }

    private UserDetailsImpl getUserRequested() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String createRequestDelete(String id){
        DevelopmentPlan developmentPlan = developPlanService.findById(id);
        if (Objects.isNull(developmentPlan)) {
            throw new CommonException("DevelopmentPlan not found");
        }
        checkAuthorityService.hasAuthorityOverOrganization(developmentPlan.getOrganizationCode());

        UserDetailsImpl userRequested = getUserRequested();
        Request request = requestService.initializeRequest(null, developmentPlan, form, getCombinedFieldMap());
        request.setCreatedBy(userRequested.getId());
        request.setReferenceId(id);
        request.setOrganizationCode(developmentPlan.getOrganizationCode());
        requestClient.save(request);

        return "Tạo yêu cầu thành công";
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        DevelopmentPlanDraft developmentPlanDraft = developPlanDraftService.findById(referenceId);
        List<DevelopmentPlanDetailDraft> developmentPlanDetailList = developPlanDetailDraftService.findByRefId(developmentPlanDraft.getId());

        DevelopmentPlan developmentPlan = DevelopmentPlan.builder()
                .organizationCode(developmentPlanDraft.getOrganizationCode())
                .name(developmentPlanDraft.getName())
                .start(developmentPlanDraft.getStart())
                .end(developmentPlanDraft.getEnd())
                .target(developmentPlanDraft.getTarget())
                .prntCode(developmentPlanDraft.getPrntCode())
                .hasChild(developmentPlanDraft.getHasChild())
                .deleted(ERecordStatus.ACTIVE.getStatus())
                .build();
        developmentPlan = developPlanService.save(developmentPlan);

        List<DevelopmentPlanDetail> developmentPlanDetails = new ArrayList<>();
        for (DevelopmentPlanDetailDraft detail: developmentPlanDetailList) {
            DevelopmentPlanDetail d = modelMapper.map(detail, DevelopmentPlanDetail.class);
            d.setRefId(developmentPlan.getId());
            developmentPlanDetails.add(d);
        }

        developmentPlanDetailClient.saveAll(developmentPlanDetails);

        developmentPlanDraft.setStatus(EApprovalStatus.APPROVED.getId());
        developmentPlanDraft.setApprovedBy(userDetails.getId());
        developPlanDraftService.save(developmentPlanDraft);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        DevelopmentPlanDraft developmentPlanDraft = developPlanDraftService.findById(referenceId);

        DevelopmentPlan developmentPlan = developPlanService.findById(developmentPlanDraft.getRefId());

        if (Objects.isNull(developmentPlan)) {
            throw new CommonException("DevelopmentPlan not found");
        }
        List<DevelopmentPlanDetailDraft> developmentPlanDetailDraftList = developmentPlanDetailDraftService.findByRefId(developmentPlanDraft.getId());
        if (Objects.isNull(developmentPlanDetailDraftList) || developmentPlanDetailDraftList.isEmpty()) {
            throw new CommonException("DevelopmentPlanDetail not found");
        }

        List<Integer> years = developmentPlanDetailDraftList.stream().map(DevelopmentPlanDetailDraft::getYear).toList();
        List<DevelopmentPlanDetail> developmentPlanDetails = developmentPlanDetailClient.findByRefIdAndYearIn(DevelopDetailRefIdRequest.builder()
                        .refId(developmentPlan.getId())
                        .years(years)
                .build()).getData();
        Map<Integer, DevelopmentPlanDetail> developmentPlanDetailMap = new HashMap<>();
        if (!developmentPlanDetails.isEmpty()) {
            for (DevelopmentPlanDetail developmentPlanDetail : developmentPlanDetails) {
                developmentPlanDetailMap.put(developmentPlanDetail.getYear(), developmentPlanDetail);
            }
        }

        Map<Integer, DevelopmentPlanDetail> developmentPlanDetailMapSave = new HashMap<>();
        List<DevelopmentPlanDetail> developmentPlanDetailListSave = getListSave(
                developmentPlanDetailMap,
                developmentPlanDetailDraftList,
                developmentPlan,
                developmentPlanDetailMapSave);
        List<DevelopmentPlanDetail> developmentPlanDetailListDelete = new ArrayList<>();
        if (!developmentPlanDetails.isEmpty()) {
            developmentPlanDetailListDelete = getListDelete(
                    developmentPlanDetails,
                    developmentPlanDetailMapSave
            );
        }

        int totalTarget = developmentPlanDetailListSave.stream().mapToInt(DevelopmentPlanDetail::getTarget).sum();
        developmentPlan.setTarget(totalTarget);
        developmentPlan.setHasChild(developmentPlanDetailListSave.size());
        developPlanService.save(developmentPlan);
        developmentPlanDetailClient.saveAll(developmentPlanDetailListSave);
        if (!developmentPlanDetailListDelete.isEmpty()) {
            developmentPlanDetailClient.deleteAll(developmentPlanDetailListDelete);
        }

        developmentPlanDraft.setApprovedBy(getUserRequested().getId());
        developmentPlanDraft.setStatus(EApprovalStatus.APPROVED.getId());
        developPlanDraftService.save(developmentPlanDraft);

        return true;
    }

    private List<DevelopmentPlanDetail> getListSave(Map<Integer, DevelopmentPlanDetail> developmentPlanDetailMap,
                                                    List<DevelopmentPlanDetailDraft> developmentPlanDetailDraftList,
                                                    DevelopmentPlan developmentPlan,
                                                    Map<Integer, DevelopmentPlanDetail> developmentPlanDetailMapSave
                                                    ){
        List<DevelopmentPlanDetail> developmentPlanDetailListSave = new ArrayList<>();

        for (DevelopmentPlanDetailDraft d : developmentPlanDetailDraftList){
            DevelopmentPlanDetail developmentPlanDetail = developmentPlanDetailMap.getOrDefault(d.getYear(), null);
            if (Objects.isNull(developmentPlanDetail)) {
                developmentPlanDetail = modelMapper.map(d, DevelopmentPlanDetail.class);
                developmentPlanDetail.setRefId(developmentPlan.getId());
            }else {
                developmentPlanDetail.setYear(d.getYear());
                developmentPlanDetail.setMin(d.getMin());
                developmentPlanDetail.setTarget(d.getTarget());
            }

            developmentPlanDetailListSave.add(developmentPlanDetail);
            developmentPlanDetailMapSave.put(d.getYear(), developmentPlanDetail);
        }
        return developmentPlanDetailListSave;
    }

    private List<DevelopmentPlanDetail> getListDelete(List<DevelopmentPlanDetail> developmentPlanDetails,
                                                      Map<Integer, DevelopmentPlanDetail> developmentPlanDetailMapSave){
        List<DevelopmentPlanDetail> developmentPlanDetailListDelete = new ArrayList<>();
        for (DevelopmentPlanDetail developmentPlanDetail : developmentPlanDetails) {
            DevelopmentPlanDetail detail = developmentPlanDetailMapSave.getOrDefault(developmentPlanDetail.getYear(), null);
            if (Objects.isNull(detail)) {
                developmentPlanDetailListDelete.add(developmentPlanDetail);
            }
        }
        return developmentPlanDetailListDelete;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        DevelopmentPlan developmentPlan = developPlanService.findById(referenceId);
        if (Objects.isNull(developmentPlan)) {
            throw new CommonException("DevelopmentPlan not found");
        }

        developmentPlan.setDeleted(ERecordStatus.DELETED.getStatus());
        developPlanService.save(developmentPlan);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        DevelopmentPlanDraft developmentPlanDraft = developPlanDraftService.findById(referenceId);
        if (Objects.isNull(developmentPlanDraft)) {
            throw new CommonException("DevelopmentPlanDraft not found");
        }

        developmentPlanDraft.setStatus(EApprovalStatus.DENIED.getId());
        developmentPlanDraft.setApprovedBy(getUserRequested().getId());
        developPlanDraftService.save(developmentPlanDraft);
    }
}
