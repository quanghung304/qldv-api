package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank.TransferWithinAgribankClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferWithinAgribankRequest;
import com.agribank.qldv_api.request.party_transfer.TransferWithinAgribankUpdateRequest;
import com.agribank.qldv_api.response.party_transfer.TransferWithinAgribankResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribankDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.dv_org.DvOrgHistoryRequest;
import com.agribank.qldvutils.request.party_transfer.ApproveTransferWithinRequest;
import com.agribank.qldvutils.request.party_transfer.TransferWithinAgribankSearch;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp30Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferWithinAgribankService implements EntityHandler {
    private final TransferWithinAgribankClient client;
    private final RequestClient requestClient;
    private final DVService dvService;
    private final RequestService requestService;
    private final TransferWithinAgribankDraftService transferWithinAgribankDraftService;
    private final OrganizationService organizationService;
    private final TransferProcessService transferProcessService;
    private final ModelMapper modelMapper;
    private final DvOrgHistoryClient dvOrgHistoryClient;

    private final EForm form = EForm.BIEU_25_TRANSFER_WITHIN_AGRIBANK;

    public Request createRequest(TransferWithinAgribankRequest request) {
        DV dv = dvService.findByStaffCode(request.getStaffCode());
        if (Objects.isNull(dv)) {
            throw new CommonException("Không tìm thấy dũ liệu. Vui lòng kiểm tra lại thông Đảng viên");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferWithinAgribankDraft transferWithinAgribankDraft = TransferWithinAgribankDraft.builder()
                .staffCode(request.getStaffCode())
                .fullName(request.getFullName())
                .decisionNumber(request.getDecisionNumber())
                .decisionDate(request.getDecisionDate())
                .decisionIssuingUnit(request.getDecisionIssuingUnit())
                .effectiveDate(request.getEffectiveDate())
                .dateOfProposal(request.getDateOfProposal())
                .numberOfDoc(request.getNumberOfDoc())
                .committeeProposalDate(request.getCommitteeProposalDate())
                .numberOfSubmission(request.getNumberOfSubmission())
                .secondIntroNumber(request.getSecondIntroNumber())
                .oldOrganizationCode(dv.getOrganizationCode())
                .build();
        transferWithinAgribankDraft.setCreatedBy(userDetails.getId());
        transferWithinAgribankDraft = transferWithinAgribankDraftService.save(transferWithinAgribankDraft);

        Request transferWithinAgribankRequest = requestService.initializeRequest(transferWithinAgribankDraft, null, form, TransferWithinAgribankDraft.FIELD_MAP);
        transferWithinAgribankRequest.setOrganizationCode(dv.getOrganizationCode());
        transferWithinAgribankRequest.setStaffCode(request.getStaffCode());
        transferWithinAgribankRequest.setCreatedBy(userDetails.getId());
        transferWithinAgribankRequest.setReferenceId(transferWithinAgribankDraft.getId());
        requestClient.save(transferWithinAgribankRequest);

        return transferWithinAgribankRequest;
    }

    public Request update(TransferWithinAgribankUpdateRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("id is required");
        }

        TransferWithinAgribank transferWithinAgribank = findById(request.getId());

        if (Objects.isNull(transferWithinAgribank)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        Organization receivingOrganizationB = null;

        if (Objects.nonNull(request.getReceivingOrgBCode())){
            receivingOrganizationB = organizationService.findByCode(request.getReceivingOrgBCode());
        }

        Organization receivingOrganizationC = null;

        if (Objects.nonNull(request.getReceivingOrgCCode())){
            receivingOrganizationC = organizationService.findByCode(request.getReceivingOrgCCode());
        }

        UserDetailsImpl userRequested = getUserRequested();

        DV dv = dvService.findByStaffCode(request.getStaffCode());

        TransferWithinAgribankDraft transferWithinAgribankDraft = modelMapper.map(request, TransferWithinAgribankDraft.class);
        transferWithinAgribankDraft.setId(null);

        if (Objects.nonNull(receivingOrganizationB)) {
            transferWithinAgribankDraft.setReceivingOrgBName(receivingOrganizationB.getName());
        }
        if (Objects.nonNull(receivingOrganizationC)) {
            transferWithinAgribankDraft.setReceivingOrgCName(receivingOrganizationC.getName());
        }

        transferWithinAgribankDraft.setCreatedBy(userRequested.getId());
        transferWithinAgribankDraft.setRefId(request.getId());
        transferWithinAgribankDraft = transferWithinAgribankDraftService.save(transferWithinAgribankDraft);

        Request transferWithinAgribankRequest = requestService.initializeRequest(transferWithinAgribankDraft, transferWithinAgribank, form, TransferWithinAgribankDraft.FIELD_MAP);
        transferWithinAgribankRequest.setOrganizationCode(dv.getOrganizationCode());
        transferWithinAgribankRequest.setStaffCode(request.getStaffCode());
        transferWithinAgribankRequest.setCreatedBy(userRequested.getId());
        transferWithinAgribankRequest.setReferenceId(transferWithinAgribankDraft.getId());
        requestClient.save(transferWithinAgribankRequest);

        return transferWithinAgribankRequest;
    }

    public TransferWithinAgribankResponse getDetail(String id){
        TransferWithinAgribank transferWithinAgribank = findById(id);
        if (Objects.isNull(transferWithinAgribank)) {
            return null;
        }

        return modelMapper.map(transferWithinAgribank, TransferWithinAgribankResponse.class);
    }

    public String createRequestDelete(String id){
        TransferWithinAgribank transferWithinAgribank = findById(id);
        if (Objects.isNull(transferWithinAgribank)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        UserDetailsImpl userRequested = getUserRequested();
        Request request = requestService.initializeRequest(null, transferWithinAgribank, form, TransferWithinAgribankDraft.FIELD_MAP);
        request.setCreatedBy(userRequested.getId());
        request.setReferenceId(id);
        request.setStaffCode(transferWithinAgribank.getStaffCode());
        request.setOrganizationCode(userRequested.getOrganizationCode());
        requestClient.save(request);

        return "Tạo yêu cầu thành công";
    }

    public PageResponse<TransferWithinAgribankResponse> search(TransferWithinAgribankSearch request){
        UserDetailsImpl userRequested = getUserRequested();
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), userRequested));
        PageResponse<TransferWithinAgribankResponse> response = new PageResponse<>();
        PageResponse<TransferWithinAgribank> transferWithinAgribankPageResponse = client.search(request).getData();
        if (Objects.isNull(transferWithinAgribankPageResponse) || transferWithinAgribankPageResponse.getData().isEmpty()) {
            return response;
        }

        response.setCurrentPage(transferWithinAgribankPageResponse.getCurrentPage());
        response.setTotalPages(transferWithinAgribankPageResponse.getTotalPages());
        response.setTotalItems(transferWithinAgribankPageResponse.getTotalItems());
        response.setData(transferWithinAgribankPageResponse.getData().stream()
                .map(t -> modelMapper.map(t, TransferWithinAgribankResponse.class)).toList());
        return response;
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        TransferWithinAgribankDraft transferWithinAgribankDraft = transferWithinAgribankDraftService.findById(referenceId);

        if (Objects.isNull(transferWithinAgribankDraft)) {
            return false;
        }

        TransferProcess transferProcess = TransferProcess.builder()
                .staffCode(transferWithinAgribankDraft.getStaffCode())
                .fullName(transferWithinAgribankDraft.getFullName())
                .transferType(ETransferType.TRANSFER_WITHIN_AGRIBANK.getId())
                .organizationCode(Constants.BTCDU_CODE)
                .status(EProcessStatus.PROCESSING.getId())
                .build();
        transferProcess = transferProcessService.save(transferProcess);

        TransferWithinAgribank transferWithinAgribank = modelMapper.map(transferWithinAgribankDraft, TransferWithinAgribank.class);
        transferWithinAgribank.setProcessId(transferProcess.getId());

        DV dv = dvService.findByStaffCode(transferWithinAgribank.getStaffCode());
        dv.setDvStatus(EDVStatus.WAITING_FOR_PARTY_ACTIVITIES_TRANSFER.getStatus());

        transferWithinAgribankDraft.setApprovedBy(userDetails.getId());
        transferWithinAgribankDraft.setStatus(EApprovalStatus.APPROVED.getId());

        DvOrgHistory history = DvOrgHistory.builder()
                .staffCode(transferWithinAgribankDraft.getStaffCode())
                .oldOrgCode(transferWithinAgribankDraft.getOldOrganizationCode())
                .newOrgCode(transferWithinAgribankDraft.getReceivingOrgBCode())
                .refId(transferProcess.getId())
                .effectiveDate(transferWithinAgribank.getEffectiveDate())
                .action(form.getCode())
                .build();

        ApproveTransferWithinRequest transferOutRequest = ApproveTransferWithinRequest.builder()
                .transfer(transferWithinAgribank)
                .process(null)
                .dv(dv)
                .draft(transferWithinAgribankDraft)
                .history(history)
                .build();
        client.saveEntities(transferOutRequest);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        TransferWithinAgribankDraft transferWithinAgribankDraft = transferWithinAgribankDraftService.findById(referenceId);

        if (Objects.isNull(transferWithinAgribankDraft)) {
            return false;
        }
        TransferProcess transferProcess = null;
        List<TransferProcess> transferProcesses = transferProcessService.findProcessingTransfer(transferWithinAgribankDraft.getStaffCode(), ETransferType.TRANSFER_WITHIN_AGRIBANK.getId());
        DV dv = dvService.findByStaffCode(transferWithinAgribankDraft.getStaffCode());
        if (
                !transferProcesses.isEmpty() &&
                        (
                                Objects.nonNull(transferWithinAgribankDraft.getSecondIntroNumber()) &&
                                        Objects.nonNull(transferWithinAgribankDraft.getTransferDate()) &&
                                        Objects.nonNull(transferWithinAgribankDraft.getReceivingOrgBCode())
                        )
        ) {
            dv.setDvStatus(EDVStatus.PARTY_MEMBER.getStatus());
            dv.setOrganizationCode(transferWithinAgribankDraft.getReceivingOrgCCode());
            transferProcess = transferProcesses.get(0);
            transferProcess.setStatus(EProcessStatus.DONE.getId());
        }

        TransferWithinAgribank transferWithinAgribank = findById(transferWithinAgribankDraft.getRefId());
        mapDraftToOfficial(transferWithinAgribank, transferWithinAgribankDraft);

        UserDetailsImpl userDetails = getUserRequested();
        transferWithinAgribankDraft.setApprovedBy(userDetails.getId());
        transferWithinAgribankDraft.setStatus(EApprovalStatus.APPROVED.getId());

        DvOrgHistoryRequest historyRequest = DvOrgHistoryRequest.builder()
                .staffCode(transferWithinAgribankDraft.getStaffCode())
                .refId(transferWithinAgribank.getProcessId())
                .build();
        DvOrgHistory history = dvOrgHistoryClient.findByStaffCodeAndRefId(historyRequest).getData();

        if (history == null) {
            throw new CommonException("Không tìm thấy dữ liệu bản ghi!");
        }

        history.setEffectiveDate(transferWithinAgribankDraft.getEffectiveDate());
        history.setOldOrgCode(transferWithinAgribankDraft.getOldOrganizationCode());
        history.setNewOrgCode(transferWithinAgribankDraft.getReceivingOrgCCode());

        ApproveTransferWithinRequest transferWithinRequest = ApproveTransferWithinRequest.builder()
                .transfer(transferWithinAgribank)
                .process(transferProcess)
                .dv(dv)
                .draft(transferWithinAgribankDraft)
                .history(history)
                .build();
        client.saveEntities(transferWithinRequest);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        TransferWithinAgribank transferWithinAgribank = findById(referenceId);
        if (Objects.isNull(transferWithinAgribank)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        transferWithinAgribank.setDeleted(ERecordStatus.DELETED.getStatus());

        client.save(transferWithinAgribank);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        TransferWithinAgribankDraft transferWithinAgribank = transferWithinAgribankDraftService.findById(referenceId);
        if (Objects.isNull(transferWithinAgribank)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        transferWithinAgribank.setStatus(EApprovalStatus.DENIED.getId());
        transferWithinAgribankDraftService.save(transferWithinAgribank);
    }


    private TransferWithinAgribank findById(String id) {
        return client.findById(id).getData().orElse(null);
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void mapDraftToOfficial(TransferWithinAgribank transfer, TransferWithinAgribankDraft draft) {
        transfer.setStaffCode(draft.getStaffCode());
        transfer.setFullName(draft.getFullName());
        transfer.setDecisionNumber(draft.getDecisionNumber());
        transfer.setDecisionDate(draft.getDecisionDate());
        transfer.setEffectiveDate(draft.getEffectiveDate());
        transfer.setDecisionIssuingUnit(draft.getDecisionIssuingUnit());
        transfer.setDateOfProposal(draft.getDateOfProposal());
        transfer.setNumberOfDoc(draft.getNumberOfDoc());
        transfer.setSecondIntroNumber(draft.getSecondIntroNumber());
        transfer.setCommitteeProposalDate(draft.getCommitteeProposalDate());
        transfer.setNumberOfSubmission(draft.getNumberOfSubmission());
        if (Objects.nonNull(draft.getTransferDate())) {
            transfer.setTransferDate(draft.getTransferDate());
        }
        if (Objects.nonNull(draft.getReceivingOrgBCode())){
            transfer.setReceivingOrgBCode(draft.getReceivingOrgBCode());
            transfer.setReceivingOrgBName(draft.getReceivingOrgBName());
        }
        if (Objects.nonNull(draft.getReceivingOrgCCode())){
            transfer.setReceivingOrgCCode(draft.getReceivingOrgCCode());
            transfer.setReceivingOrgCName(draft.getReceivingOrgCName());
        }
    }

    public PageResponse<DvRp30Response> search30(SearchRpRequest request){
        return client.searchRp30(request).getData();
    }


    public TransferWithinAgribankResponse getDraftDetail(String id){
        TransferWithinAgribankDraft draft = transferWithinAgribankDraftService.findById(id);
        if (Objects.isNull(draft)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        return modelMapper.map(draft, TransferWithinAgribankResponse.class);
    }

    @SneakyThrows
    public RequestResponse updateDraft(TransferWithinAgribankUpdateRequest request){
        if (Objects.isNull(request.getId()) || request.getId().isBlank()) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        TransferWithinAgribankDraft transferWithinAgribankDraft = transferWithinAgribankDraftService.findById(request.getId());
        if (Objects.isNull(transferWithinAgribankDraft)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        Request requestTransferWithinAgri = requestClient.findByReferenceId(request.getId()).getData();
        if (Objects.isNull(requestTransferWithinAgri)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        if (EApprovalStatus.PENDING.getId() != requestTransferWithinAgri.getStatus()) {
            throw new CommonException(ExceptionMessage.REQUEST_NOT_PENDING);
        }

        DV dv = dvService.findByStaffCode(request.getStaffCode());

        if (Objects.isNull(dv)) {
            throw new CommonException(ExceptionMessage.MEMBER_NOT_FOUND);
        }

        Organization receivingOrganizationB = null;

        if (Objects.nonNull(request.getReceivingOrgBCode())){
            receivingOrganizationB = organizationService.findByCode(request.getReceivingOrgBCode());
        }

        Organization receivingOrganizationC = null;

        if (Objects.nonNull(request.getReceivingOrgCCode())){
            receivingOrganizationC = organizationService.findByCode(request.getReceivingOrgCCode());
        }

        UserDetailsImpl userRequested = getUserRequested();

        transferWithinAgribankDraft.setOldOrganizationCode(dv.getOrganizationCode());
        transferWithinAgribankDraft.setCreatedBy(userRequested.getId());

        if (Objects.nonNull(receivingOrganizationB)) {
            transferWithinAgribankDraft.setReceivingOrgBName(receivingOrganizationB.getName());
        }

        if (Objects.nonNull(receivingOrganizationC)) {
            transferWithinAgribankDraft.setReceivingOrgCName(receivingOrganizationC.getName());
        }

        mapTransferWithinAgribankDraft(transferWithinAgribankDraft, request);

        requestTransferWithinAgri.setNewData(requestService.createJsonData(transferWithinAgribankDraft, TransferWithinAgribankDraft.FIELD_MAP));
        requestTransferWithinAgri.setCreatedBy(userRequested.getId());

        transferWithinAgribankDraftService.save(transferWithinAgribankDraft);
        requestClient.save(requestTransferWithinAgri);

        return modelMapper.map(requestTransferWithinAgri, RequestResponse.class);
    }

    private void mapTransferWithinAgribankDraft(TransferWithinAgribankDraft transferWithinAgribankDraft, TransferWithinAgribankUpdateRequest request){
        transferWithinAgribankDraft.setStaffCode(request.getStaffCode());
        transferWithinAgribankDraft.setFullName(request.getFullName());
        transferWithinAgribankDraft.setProcessId(request.getProcessId());
        transferWithinAgribankDraft.setDecisionNumber(request.getDecisionNumber());
        transferWithinAgribankDraft.setExpectedExpiryDate(request.getExpectedExpiryDate());
        transferWithinAgribankDraft.setDecisionDate(request.getDecisionDate());
        transferWithinAgribankDraft.setDecisionIssuingUnit(request.getDecisionIssuingUnit());
        transferWithinAgribankDraft.setEffectiveDate(request.getEffectiveDate());
        transferWithinAgribankDraft.setDateOfProposal(request.getDateOfProposal());
        transferWithinAgribankDraft.setNumberOfDoc(request.getNumberOfDoc());
        transferWithinAgribankDraft.setCommitteeProposalDate(request.getCommitteeProposalDate());
        transferWithinAgribankDraft.setSecondIntroNumber(request.getSecondIntroNumber());
        transferWithinAgribankDraft.setTransferDate(request.getTransferDate());
        transferWithinAgribankDraft.setReceivingOrgBCode(request.getReceivingOrgBCode());
        transferWithinAgribankDraft.setReceivingOrgCCode(request.getReceivingOrgCCode());
    }
}
