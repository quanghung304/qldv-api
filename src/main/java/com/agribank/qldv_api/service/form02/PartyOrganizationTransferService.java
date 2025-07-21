package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.form02.transfer.PartyOrgTransferDetailClient;
import com.agribank.qldv_api.gateway.form02.transfer.PartyOrgTransferDetailDraftClient;
import com.agribank.qldv_api.gateway.form02.transfer.PartyOrganizationTransferClient;
import com.agribank.qldv_api.gateway.form02.transfer.PartyOrganizationTransferDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.PartyOrgTransferRequest;
import com.agribank.qldv_api.response.form02.transfer.PartyOrgTranDetailResponse;
import com.agribank.qldv_api.response.form02.transfer.PartyOrgTranResponse;
import com.agribank.qldv_api.response.form02.transfer.PartyOrganizationTransferResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.DvOrgService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrgTransferDetail;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrgTransferDetailDraft;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrganizationTransfer;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrganizationTransferDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.dv_org_history.DvOrgHisListRequest;
import com.agribank.qldvutils.request.dv_org_history.DvOrgHisRequest;
import com.agribank.qldvutils.request.form02.SearchPartyOrgTransferRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranDraftRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranEntityCreateRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranEntityUpdateRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class PartyOrganizationTransferService implements EntityHandler {
    private final PartyOrganizationTransferClient client;
    private final RequestService requestService;
    private final PartyOrganizationTransferDraftClient partyOrganizationTransferDraftClient;
    private final PartyOrgTransferDetailClient partyOrgTransferDetailClient;
    private final PartyOrgTransferDetailDraftClient partyOrgTransferDetailDraftClient;
    private final OrganizationService organizationService;
    private final DVService dvService;
    private final DvOrgService dvOrgService;
    private final EForm form = EForm.BIEU_02_TRANSFER;
    private final ModelMapper modelMapper;

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(PartyOrganizationTransferDraft.BASE_FIELD_MAP);

        return combinedFieldMap;
    }

    public RequestResponse create(PartyOrgTransferRequest request) {
        PartyOrgTranDraftRequest entity = new PartyOrgTranDraftRequest();
        List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts = new ArrayList<>();
        PartyOrganizationTransferDraft partyOrganizationTransferDraft = getPartyOrganizationTransferDraft(request, partyOrgTransferDetailDrafts);

        Request transferRequest = requestService.initializeRequest(
                partyOrganizationTransferDraft,
                null,
                form,
                getCombinedFieldMap());

        transferRequest.setNewData(CommonUtils.createJsonData(partyOrganizationTransferDraft, partyOrgTransferDetailDrafts, PartyOrgTransferDetailDraft.BASE_FIELD_MAP, getCombinedFieldMap()));
        transferRequest.setCreatedBy(getUserRequested().getId());
        transferRequest.setOrganizationCode(request.getReceivingOrgCode());

        entity.setPartyOrganizationTransferDraft(partyOrganizationTransferDraft);
        entity.setPartyOrgTransferDetailDrafts(partyOrgTransferDetailDrafts);
        entity.setRequest(transferRequest);
        client.saveEntityDraft(entity);
        return modelMapper.map(transferRequest, RequestResponse.class);
    }

    private PartyOrganizationTransferDraft getPartyOrganizationTransferDraft(PartyOrgTransferRequest request, List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts){
        Organization receivingOrg = getOrganization(request.getReceivingOrgCode(), "Không tồn tại Chi đảng bộ: " + request.getReceivingOrgCode());

        getOrganization(request.getDecisionCommittee(), "Không tồn tại cấp ủy quyết định này");

        List<String> organizationCodes = request.getPartyOrgTransfers();

        List<Organization> organizationTransfers = organizationService.findAllByCode(organizationCodes);
        if (organizationTransfers.isEmpty()){
            throw new CommonException("Kiểm tra Chi Đảng bộ được chuyển giao, không tìm thấy dữ liệu");
        }

        PartyOrganizationTransferDraft partyOrganizationTransferDraft = PartyOrganizationTransferDraft.builder()
                .receivingOrgCode(receivingOrg.getCode())
                .receivingOrgName(receivingOrg.getName())
                .build();
        partyOrganizationTransferDraft.setDecisionCommittee(request.getDecisionCommittee());

        if (request.getPartyOrgTransfers().isEmpty()){
            return partyOrganizationTransferDraft;
        }

        setPartyOrgTranDetailDraftsAndSetOrgCode(receivingOrg, organizationTransfers, partyOrgTransferDetailDrafts);

        return partyOrganizationTransferDraft;
    }

    private void setPartyOrgTranDetailDraftsAndSetOrgCode(Organization receivingOrg, List<Organization> organizationTransfers, List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts){
        Organization receivingOrgChildMax = organizationService.getOrganizationChildMax(receivingOrg.getCode(), receivingOrg.getForm());
        String codeChild = null;
        if (Objects.nonNull(receivingOrgChildMax)){
            codeChild = receivingOrgChildMax.getCode();
        }

        for (Organization organization : organizationTransfers) {
            if (receivingOrg.getForm().substring(0, 1).compareTo(organization.getForm().substring(0,1)) > 0){
                throw new CommonException(String.format("Chi đảng bộ %s không thể tiếp nhận chi đảng bộ %s", receivingOrg.getName(), organization.getName()));
            }

            if (receivingOrg.getForm().substring(0, 1).equals(organization.getForm().substring(0, 1))) {
                throw new CommonException(String.format("Chi đảng bộ cùng cấp %s không thể tiếp nhận chi đảng bộ %s", receivingOrg.getName(), organization.getName()));
            }

            PartyOrgTransferDetailDraft partyOrgTransferDetailDraft = PartyOrgTransferDetailDraft.builder()
                    .oldParentCode(organization.getParentCode())
                    .organizationCode(organization.getCode())
                    .organizationName(organization.getName())
                    .form(organization.getForm())
                    .build();

            //Trường hợp update
            if (receivingOrg.getCode().equals(organization.getParentCode())){
                partyOrgTransferDetailDraft.setNewOrganizationCode(organization.getCode());
                partyOrgTransferDetailDrafts.add(partyOrgTransferDetailDraft);
                continue;
            }
            //gán code
            if (Objects.isNull(codeChild)){
                codeChild = receivingOrg.getCode()+"01";
                partyOrgTransferDetailDraft.setNewOrganizationCode(codeChild);
            }else {
                Integer code = Integer.parseInt(codeChild) + 1;
                partyOrgTransferDetailDraft.setNewOrganizationCode(code+"");
                codeChild = String.valueOf(code);
            }

            partyOrgTransferDetailDrafts.add(partyOrgTransferDetailDraft);
        }
    }

    private Organization getOrganization(String code, String messageError){
        Organization organization = organizationService.findByCode(code);
        if (Objects.isNull(organization)){
            throw new CommonException(messageError);
        }
        return organization;
    }

    public RequestResponse update(PartyOrgTransferRequest request){
        if (Objects.isNull(request.getId())){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        PartyOrganizationTransfer partyOrganizationTransfer = findById(request.getId());
        if (Objects.isNull(partyOrganizationTransfer)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        PartyOrgTranDraftRequest entity = new PartyOrgTranDraftRequest();
        List<PartyOrgTransferDetail> oldPartyOrgTransferDetails = partyOrgTransferDetailClient.findByRefId(partyOrganizationTransfer.getId()).getData();

        List<String> organizationCodes = getPartyOrgTransfers(request, oldPartyOrgTransferDetails);

        request.setPartyOrgTransfers(organizationCodes);
        List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts = new ArrayList<>();

        PartyOrganizationTransferDraft partyOrganizationTransferDraft = getPartyOrganizationTransferDraft(request, partyOrgTransferDetailDrafts);
        partyOrganizationTransferDraft.setRefId(partyOrganizationTransfer.getId());

        Request transferRequest = requestService.initializeRequest(
                partyOrganizationTransferDraft,
                partyOrganizationTransfer,
                form,
                getCombinedFieldMap());

        transferRequest.setNewData(CommonUtils.createJsonData(partyOrganizationTransferDraft, partyOrgTransferDetailDrafts, PartyOrgTransferDetailDraft.BASE_FIELD_MAP, getCombinedFieldMap()));
        transferRequest.setOldData(CommonUtils.createJsonData(partyOrganizationTransfer, oldPartyOrgTransferDetails, PartyOrgTransferDetailDraft.BASE_FIELD_MAP, getCombinedFieldMap()));
        transferRequest.setCreatedBy(getUserRequested().getId());
        transferRequest.setOrganizationCode(request.getReceivingOrgCode());

        entity.setPartyOrganizationTransferDraft(partyOrganizationTransferDraft);
        entity.setPartyOrgTransferDetailDrafts(partyOrgTransferDetailDrafts);
        entity.setRequest(transferRequest);
        client.saveEntityDraft(entity);
        return modelMapper.map(transferRequest, RequestResponse.class);
    }

    private List<String> getPartyOrgTransfers(PartyOrgTransferRequest request, List<PartyOrgTransferDetail> oldPartyOrgTransferDetails){
        if (request.getPartyOrgTransfers().isEmpty()){
            return new ArrayList<>();
        }
        Map<String, PartyOrgTransferDetail> detailMap = new HashMap<>();
        for (PartyOrgTransferDetail partyOrgTransferDetail : oldPartyOrgTransferDetails) {
            detailMap.put(partyOrgTransferDetail.getOrganizationCode(), partyOrgTransferDetail);
        }

        List<String> organizationCodes = new ArrayList<>();
        for (String partyOrgTransfers : request.getPartyOrgTransfers()) {
            PartyOrgTransferDetail detail = detailMap.getOrDefault(partyOrgTransfers, null);
            if (Objects.nonNull(detail)){
                organizationCodes.add(detail.getNewOrganizationCode());
            }else {
                organizationCodes.add(partyOrgTransfers);
            }
        }
        return organizationCodes;
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        PartyOrganizationTransferDraft partyOrganizationTransferDraft = partyOrganizationTransferDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));
        PartyOrganizationTransfer partyOrgTransfer = new PartyOrganizationTransfer();

        mapPartyOrganizationTransfer(partyOrganizationTransferDraft, partyOrgTransfer);

        List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts = partyOrgTransferDetailDraftClient.findByRefId(partyOrganizationTransferDraft.getId()).getData();
        if (partyOrgTransferDetailDrafts.isEmpty()){
            return true;
        }
        // Biến TCD mới được chuyển giao
        List<Organization> newOrganizations = new ArrayList<>();
        Map<String, Organization> newOrganizationMap = new HashMap<>();
        // Lấy Mã TCD Cũ để cho ngừng hoạt động
        List<String> organizationNCodes = new ArrayList<>();
        List<PartyOrgTransferDetail> partyOrgTransferDetails = processPartyOrgTransfer(
                organizationNCodes,
                newOrganizations,
                newOrganizationMap,
                partyOrganizationTransferDraft,
                partyOrgTransferDetailDrafts);

        List<DV> dvUpdateOrgNew = new ArrayList<>();
        List<OrganizationHistory> organizationHistories = createHistoryOrg(organizationNCodes, dvUpdateOrgNew, partyOrganizationTransferDraft);
        List<DvOrgHistory> dvOrgHistories = createHistoryDV(dvUpdateOrgNew, newOrganizationMap, partyOrganizationTransferDraft);

        PartyOrgTranEntityCreateRequest entity = PartyOrgTranEntityCreateRequest.builder()
                .partyOrgTransfer(partyOrgTransfer)
                .partyOrgTransferDetails(partyOrgTransferDetails)
                .organizationHistories(organizationHistories)
                .organizations(newOrganizations)
                .dvOrgHistories(dvOrgHistories)
                .dvs(dvUpdateOrgNew)
                .build();

        return client.createEntity(entity).getData();
    }

    public List<PartyOrgTransferDetail> processPartyOrgTransfer(
            List<String> organizationNCodes,
            List<Organization> newOrganizations,
            Map<String, Organization> newOrganizationMap,
            PartyOrganizationTransferDraft partyOrganizationTransferDraft,
            List<PartyOrgTransferDetailDraft> partyOrgTransferDetailDrafts
    ) {
        List<PartyOrgTransferDetail> partyOrgTransferDetails = new ArrayList<>();
        for (PartyOrgTransferDetailDraft partyOrgTransferDetailDraft : partyOrgTransferDetailDrafts) {
            organizationNCodes.add(partyOrgTransferDetailDraft.getOrganizationCode());

            // Khởi tạo tổ chức Đảng mới
            Organization organization = Organization.builder()
                    .authorized(1)
                    .name(partyOrgTransferDetailDraft.getOrganizationName())
                    .parentCode(partyOrganizationTransferDraft.getReceivingOrgCode())
                    .form(partyOrgTransferDetailDraft.getForm())
                    .resolutionNumber(partyOrganizationTransferDraft.getConclusionNumber())
                    .resolutionDate(partyOrganizationTransferDraft.getConclusionDate())
                    .decisionDate(partyOrganizationTransferDraft.getDecisionDate())
                    .effectiveDate(partyOrganizationTransferDraft.getEffectiveDate())
                    .status("Y")
                    .build();
            organization.setCode(partyOrgTransferDetailDraft.getNewOrganizationCode());

            newOrganizations.add(organization);
            newOrganizationMap.put(partyOrgTransferDetailDraft.getOrganizationCode(), organization);

            PartyOrgTransferDetail partyOrgTransferDetail = modelMapper.map(partyOrgTransferDetailDraft, PartyOrgTransferDetail.class);
            partyOrgTransferDetails.add(partyOrgTransferDetail);
        }

        return partyOrgTransferDetails;
    }

    private List<OrganizationHistory> createHistoryOrg(List<String> organizationNCodes, List<DV> dvUpdateOrgNew,
                                                       PartyOrganizationTransferDraft partyOrganizationTransferDraft){
        if (organizationNCodes.isEmpty()){
            return new ArrayList<>();
        }
        List<OrganizationHistory> organizationHistories = new ArrayList<>();
        //lấy mã chi DDangr bộ và update ve trang thái ngừng hoạt động
        List<Organization> organizationUpdateStatusNs = organizationService.findAllByCode(organizationNCodes);

        for (Organization organization : organizationUpdateStatusNs) {
            //Lấy toàn bộ Đảng viên của TCD cha và con của nó
            List<DV> dvOrg = dvService.getActiveDVByOrganizationCode(organization.getCode());
            if (!dvOrg.isEmpty()){
                dvUpdateOrgNew.addAll(dvOrg);
            }

            //Cập nhật lịch sử thay đổi TCD
            OrganizationHistory organizationHistory = OrganizationHistory.builder()
                    .code(organization.getCode())
                    .name(organization.getName())
                    .type(EReport01Type.TRANSFER.getId())
                    .effectiveDate(partyOrganizationTransferDraft.getEffectiveDate())
                    .lastAction("Y")
                    .build();

            organizationHistories.add(organizationHistory);
        }
        return organizationHistories;
    }

    private List<DvOrgHistory> createHistoryDV(List<DV> dvUpdateOrgNew, Map<String, Organization> newOrganizationMap, PartyOrganizationTransferDraft partyOrganizationTransferDraft){
        if (dvUpdateOrgNew.isEmpty()){
            return new ArrayList<>();
        }
        //Cập nhật lại TCD mới cho DV và ghi lịch sử
        List<DvOrgHistory> dvOrgHistories = dvOrgService.findByStaffCodesAndRefId(DvOrgHisListRequest.builder()
                .refId(partyOrganizationTransferDraft.getRefId())
                        .staffCodes(dvUpdateOrgNew.stream().map(DV::getStaffCode).toList())
                .build());
        Map<String, DvOrgHistory> dvOrgHistoryMap = new HashMap<>();
        if ( !dvOrgHistories.isEmpty()){
            for (DvOrgHistory dvOrgHistory : dvOrgHistories) {
                dvOrgHistoryMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }
        }
        for (DV dv : dvUpdateOrgNew) {
            Organization newOrg = newOrganizationMap.getOrDefault(dv.getOrganizationCode(), null);
            DvOrgHistory dvOrgHistory = dvOrgHistoryMap.getOrDefault(dv.getStaffCode(), new DvOrgHistory());
            if (Objects.nonNull(newOrg)){
                dvOrgHistory.setStaffCode(dv.getStaffCode());
                dvOrgHistory.setOldOrgCode(dv.getOrganizationCode());
                dvOrgHistory.setNewOrgCode(newOrg.getCode());
                dvOrgHistory.setEffectiveDate(partyOrganizationTransferDraft.getEffectiveDate());
                dvOrgHistory.setAction(EReport01Type.TRANSFER.getId()+"");

                dvOrgHistories.add(dvOrgHistory);
                dv.setOrganizationCode(newOrg.getCode());
            }
        }

        return dvOrgHistories;
    }

    private void mapPartyOrganizationTransfer(PartyOrganizationTransferDraft partyOrganizationTransferDraft, PartyOrganizationTransfer partyOrganizationTransfer){
        if (Objects.nonNull(partyOrganizationTransferDraft.getDecisionCommittee())){
            partyOrganizationTransfer.setDecisionCommittee(partyOrganizationTransferDraft.getDecisionCommittee());
        }

        if(Objects.nonNull(partyOrganizationTransferDraft.getReceivingOrgCode())){
            partyOrganizationTransfer.setReceivingOrgCode(partyOrganizationTransferDraft.getReceivingOrgCode());
        }

        if (Objects.nonNull(partyOrganizationTransferDraft.getReceivingOrgName())){
            partyOrganizationTransfer.setReceivingOrgName(partyOrganizationTransferDraft.getReceivingOrgName());
        }
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        PartyOrganizationTransferDraft draft = partyOrganizationTransferDraftClient.findById(referenceId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy bản ghi"));

        PartyOrganizationTransfer partyOrg = findById(draft.getRefId());
        if (Objects.isNull(partyOrg)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        List<PartyOrgTransferDetail> partyOrgTransferDetails = partyOrgTransferDetailClient.findByRefId(partyOrg.getId()).getData();

        List<PartyOrgTransferDetailDraft> detailDrafts = partyOrgTransferDetailDraftClient.findByRefId(draft.getId()).getData();

        PartyOrgTranEntityUpdateRequest entity = new PartyOrgTranEntityUpdateRequest();
        mapPartyOrganizationTransfer(draft, partyOrg);
        entity.setPartyOrgTransfer(partyOrg);
        if (detailDrafts.isEmpty()){
            return client.updateEntity(entity).getData();
        }

        Map<String, PartyOrgTransferDetailDraft> detailDraftMap = new HashMap<>();
        for (PartyOrgTransferDetailDraft partyOrgTransferDetailDraft : detailDrafts) {
            detailDraftMap.put(partyOrgTransferDetailDraft.getOrganizationCode(), partyOrgTransferDetailDraft);
        }

        //Khôi phục đã xóa tcd cú, dv cũ, tắt trạng thái tcd mới
        List<PartyOrgTransferDetail> detailsDeleted = new ArrayList<>();
        List<String> orgCodes = new ArrayList<>();
        Map<String, PartyOrgTransferDetail> partyOrgTransferDetailHashMap = new HashMap<>();
        for (PartyOrgTransferDetail partyOrgTransferDetail : partyOrgTransferDetails) {
            PartyOrgTransferDetailDraft draftDetail = detailDraftMap.getOrDefault(partyOrgTransferDetail.getNewOrganizationCode(), null);
            if (Objects.isNull(draftDetail)){
                detailsDeleted.add(partyOrgTransferDetail);
                orgCodes.add(partyOrgTransferDetail.getNewOrganizationCode());
            }else {
                partyOrgTransferDetailHashMap.put(partyOrgTransferDetail.getNewOrganizationCode(), partyOrgTransferDetail);
            }
        }

        //Cập nhật trạng thái TCD mới
        rollbackOrgs(orgCodes, detailsDeleted, partyOrgTransferDetails, entity);

        //Khôi phục Đảng viên về tcd cũ
        rollbackDvFromOrgOld(orgCodes, partyOrg, entity);
        mapPartyOrganizationTransfer(draft, partyOrg);

        // Biến TCD mới được chuyển giao
        List<Organization> newOrganizations = new ArrayList<>();
        Map<String, Organization> newOrganizationMap = new HashMap<>();
        // Lấy Mã TCD Cũ để cho ngừng hoạt động
        List<String> organizationNCodes = new ArrayList<>();

        List<PartyOrgTransferDetail> detailRequest = processPartyOrgTransfer(
                organizationNCodes,
                newOrganizations,
                newOrganizationMap,
                draft,
                detailDrafts);

        for (PartyOrgTransferDetail partyOrgTransferDetail : detailRequest) {
            PartyOrgTransferDetail detail = partyOrgTransferDetailHashMap.getOrDefault(partyOrgTransferDetail.getNewOrganizationCode(), null);
            if (Objects.nonNull(detail)){
                partyOrgTransferDetail.setId(detail.getId());
                partyOrgTransferDetail.setOrganizationCode(detail.getOrganizationCode());
                partyOrgTransferDetail.setOrganizationName(detail.getOrganizationName());
            }
        }

        // Lưu danh sách chi tiết chuyển giao tổ chức Đảng
        entity.setPartyOrgTransferDetails(detailRequest);
        List<DV> dvUpdateOrgNew = new ArrayList<>();
        //Tạo mới TCD và cập nhật lịch sử thay đổi
        List<OrganizationHistory> organizationHistories = createHistoryOrg(organizationNCodes, dvUpdateOrgNew, draft);
        //Cập nhật lại TCD mới cho DV và ghi lịch sử
        List<DvOrgHistory> dvOrgHistories = createHistoryDV(dvUpdateOrgNew, newOrganizationMap, draft);

        entity.setDvOrgHistories(dvOrgHistories);
        entity.setOrganizations(newOrganizations);
        entity.setOrganizationHistories(organizationHistories);
        entity.setDvs(dvUpdateOrgNew);
        return client.updateEntity(entity).getData();
    }

    private void rollbackOrgs(List<String> orgCodes,
                              List<PartyOrgTransferDetail> detailsDeleted,
                              List<PartyOrgTransferDetail> partyOrgTransferDetails,
                              PartyOrgTranEntityUpdateRequest entity){
        if (orgCodes.isEmpty()){
            return;
        }

        //xóa bản ghi thừa trong detail
        entity.setDetailsDeleted(detailsDeleted);
        partyOrgTransferDetails.removeAll(detailsDeleted);
    }

    private void rollbackDvFromOrgOld(List<String> orgCodes, PartyOrganizationTransfer partyOrg, PartyOrgTranEntityUpdateRequest entity){
        if (orgCodes.isEmpty()){
            return;
        }
        //Khôi phục Đảng viên về tcd cũ
        List<DvOrgHistory> dvOrgHistories = dvOrgService.getOrgHis(DvOrgHisRequest.builder()
                .newOrgCodes(orgCodes)
                .refId(partyOrg.getId())
                .action(EReport01Type.TRANSFER.getId()+"")
                .build());
        Map<String, DvOrgHistory> dvOrgHistoryMap = new HashMap<>();
        if (!dvOrgHistories.isEmpty()){
            for (DvOrgHistory dvOrgHistory : dvOrgHistories) {
                dvOrgHistoryMap.put(dvOrgHistory.getStaffCode(), dvOrgHistory);
            }

            List<DV> dvResets = dvService.findByOrganizationCodeActiveIn(orgCodes);
            List<DvOrgHistory> dvOrgHistoryUpdates = new ArrayList<>();
            for (DV dv : dvResets) {
                DvOrgHistory dvOrgHistory = dvOrgHistoryMap.get(dv.getStaffCode());
                dv.setOrganizationCode(dvOrgHistory.getOldOrgCode());

                dvOrgHistory.setNewOrgCode(dv.getOrganizationCode());
                dvOrgHistory.setOldOrgCode(dvOrgHistory.getNewOrgCode());
                dvOrgHistoryUpdates.add(dvOrgHistory);
            }
            //Cập nhật lại dữ liệu
            entity.setDvResets(dvResets);
            entity.setDvOrgHistoryReset(dvOrgHistoryUpdates);
        }
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {

    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    private PartyOrganizationTransfer findById(String id){
        return client.findById(id).getData().orElse(null);
    }

    public PartyOrgTranResponse get(String id){
        PartyOrganizationTransfer partyOrganizationTransfer = findById(id);
        if (Objects.isNull(partyOrganizationTransfer)){
            return null;
        }

        PartyOrgTranResponse response = PartyOrgTranResponse.builder()
                .id(partyOrganizationTransfer.getId())
                .receivingOrgName(partyOrganizationTransfer.getReceivingOrgName())
                .receivingOrgCode(partyOrganizationTransfer.getReceivingOrgCode())
                .decisionCommittee(partyOrganizationTransfer.getDecisionCommittee())
                .build();

        Organization organizationReference = organizationService.findByCode(response.getDecisionCommittee());
        if (Objects.nonNull(organizationReference)){
            response.setDecisionCommitteeName(organizationReference.getName());
        }

        List<PartyOrgTransferDetail> partyOrgTransferDetails = partyOrgTransferDetailClient.findByRefId(partyOrganizationTransfer.getId()).getData();
        List<PartyOrgTranDetailResponse> detailResponses = new ArrayList<>();
        if (!partyOrgTransferDetails.isEmpty()){
            detailResponses = partyOrgTransferDetails.stream().map(
                    p -> PartyOrgTranDetailResponse.builder()
                            .organizationCode(p.getOrganizationCode())
                            .organizationName(p.getOrganizationName())
                            .build()).toList();
        }
        response.setPartyOrgTranDetails(detailResponses);

        return response;
    }

    public PageResponse<PartyOrganizationTransferResponse> search(SearchPartyOrgTransferRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<PartyOrganizationTransferResponse> response = new PageResponse<>();
        PageResponse<PartyOrganizationTransfer> pageResponse = client.search(request).getData();
        if (Objects.isNull(pageResponse.getData()) || pageResponse.getData().isEmpty()){
            return response;
        }

        response.setCurrentPage(pageResponse.getCurrentPage());
        response.setTotalPages(pageResponse.getTotalPages());
        response.setTotalItems(pageResponse.getTotalItems());
        response.setData(pageResponse.getData().stream()
                .map(p -> modelMapper.map(p, PartyOrganizationTransferResponse.class)).toList());
        return response;
    }
}
