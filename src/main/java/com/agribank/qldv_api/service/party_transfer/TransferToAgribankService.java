package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferToAgribankRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribankDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.dv_org.DvOrgHistoryRequest;
import com.agribank.qldvutils.request.party_transfer.ApproveTransferToRequest;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferToAgribankService implements EntityHandler {
    ModelMapper modelMapper;

    OrganizationClient organizationClient;
    TransferToAgribankClient transferToAgribankClient;
    TransferToAgribankDraftClient transferToDraftClient;
    TransferProcessClient transferProcessClient;
    RequestClient requestClient;
    DvOrgHistoryClient dvOrgHistoryClient;

    RequestService requestService;

    EForm form = EForm.BIEU_25_TRANSFER_TO_AGRIBANK;

    public Request createRequest(TransferToAgribankRequest request) {
        List<TransferToAgribankDraft> drafts = transferToDraftClient.findByStaffCode(request.getStaffCode()).getData();

        if (!drafts.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu chuyển sinh hoạt đảng cho cán bộ");
        }

        Organization receivingOrganization = organizationClient.findByCode(request.getReceivingOrgBCode()).getData();

       if (Objects.isNull(receivingOrganization)) {
           throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
       }

       UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

       TransferToAgribankDraft transferToAgribank = modelMapper.map(request, TransferToAgribankDraft.class);
       transferToAgribank.setReceivingOrgBName(receivingOrganization.getName());
       transferToAgribank.setCreatedBy(userDetails.getId());
       transferToAgribank = transferToDraftClient.save(transferToAgribank).getData();

       Request transferToAgribankRequest = requestService.initializeRequest(transferToAgribank, null, form, TransferToAgribankDraft.FIELD_MAP);
       transferToAgribankRequest.setOrganizationCode(userDetails.getOrganizationCode());
       transferToAgribankRequest.setStaffCode(request.getStaffCode());
       transferToAgribankRequest.setCreatedBy(userDetails.getId());
       transferToAgribankRequest.setReferenceId(transferToAgribank.getId());
       requestClient.save(transferToAgribankRequest);

       return transferToAgribankRequest;
    }


    public PageResponse<TransferToAgribank> getList(TransferToFilterRequest request) {
        return transferToAgribankClient.getList(request).getData();
    }


    public TransferToAgribank getDetail(String id) {
        return transferToAgribankClient.findById(id).getData().orElse(null);
    }

    public Request update(TransferToAgribankRequest request) {
        TransferToAgribank transferToAgribank = transferToAgribankClient.findById(request.getId()).getData().orElse(null);

        if (Objects.isNull(transferToAgribank)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        Organization receivingOrganizationB = organizationClient.findByCode(request.getReceivingOrgBCode()).getData();

        if (Objects.isNull(receivingOrganizationB)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferToAgribankDraft transferToAgribankDraft = modelMapper.map(request, TransferToAgribankDraft.class);
        transferToAgribankDraft.setId(null);
        transferToAgribankDraft.setReceivingOrgBName(receivingOrganizationB.getName());
        transferToAgribankDraft.setCreatedBy(userDetails.getId());

        if (Objects.nonNull(request.getReceivingOrgCCode())) {
            Organization receivingOrganizationC = organizationClient.findByCode(request.getReceivingOrgCCode()).getData();

            if (Objects.isNull(receivingOrganizationC)) {
                throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
            }

            transferToAgribankDraft.setReceivingOrgCName(receivingOrganizationC.getName());
        }

        transferToAgribankDraft = transferToDraftClient.save(transferToAgribankDraft).getData();

        Request transferToAgribankRequest = requestService.initializeRequest(transferToAgribankDraft, transferToAgribank, form, TransferToAgribankDraft.FIELD_MAP);
        transferToAgribankRequest.setOrganizationCode(userDetails.getOrganizationCode());
        transferToAgribankRequest.setStaffCode(request.getStaffCode());
        transferToAgribankRequest.setCreatedBy(userDetails.getId());
        transferToAgribankRequest.setReferenceId(transferToAgribankDraft.getId());
        requestClient.save(transferToAgribankRequest);

        return transferToAgribankRequest;
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransferToAgribankDraft transferToAgribankDraft = transferToDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(transferToAgribankDraft)) {
            return false;
        }

        TransferProcess transferProcess = TransferProcess.builder()
                .staffCode(transferToAgribankDraft.getStaffCode())
                .fullName(transferToAgribankDraft.getFullName())
                .transferType(ETransferType.TRANSFER_TO_AGRIBANK.getId())
                .organizationCode(transferToAgribankDraft.getReceivingOrgBCode())
                .status(EProcessStatus.PROCESSING.getId())
                .build();
        transferProcess = transferProcessClient.save(transferProcess).getData();

        TransferToAgribank transferToAgribank = modelMapper.map(transferToAgribankDraft, TransferToAgribank.class);
        transferToAgribank.setProcessId(transferProcess.getId());

        DV dv = DV.builder()
                .staffCode(transferToAgribank.getStaffCode())
                .fullName(transferToAgribank.getFullName())
                .dvStatus(EDVStatus.PARTY_MEMBER.getStatus())
                .organizationCode(transferToAgribank.getReceivingOrgBCode())
                .build();

        transferToAgribankDraft.setApprovedBy(userDetails.getId());
        transferToAgribankDraft.setStatus(EApprovalStatus.APPROVED.getId());

        DvOrgHistory history = DvOrgHistory.builder()
                .staffCode(transferToAgribankDraft.getStaffCode())
                .oldOrgCode(null)
                .newOrgCode(transferToAgribank.getReceivingOrgBCode())
                .refId(transferProcess.getId())
                .effectiveDate(transferToAgribankDraft.getEffectiveDate())
                .action(form.getCode())
                .build();

        ApproveTransferToRequest transferOutRequest = ApproveTransferToRequest.builder()
                .transfer(transferToAgribank)
                .process(null)
                .dv(dv)
                .draft(transferToAgribankDraft)
                .history(history)
                .build();
        transferToAgribankClient.saveEntities(transferOutRequest);

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        TransferToAgribankDraft transferToAgribankDraft = transferToDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(transferToAgribankDraft)) {
            return false;
        }

        TransferProcess transferProcess = null;
        List<TransferProcess> transferProcesses = transferProcessClient.findProcessingTransfer(transferToAgribankDraft.getStaffCode(), ETransferType.TRANSFER_TO_AGRIBANK.getId())
                .getData();

        //dua dang vien ra khoi danh sach dang csh dang khi dang bo cap B gan dang vien ve chi bo cap C
        if (!transferProcesses.isEmpty() && Objects.nonNull(transferToAgribankDraft.getReceivingOrgCCode())) {
            transferProcess = transferProcesses.get(0);
            transferProcess.setStatus(EProcessStatus.DONE.getId());
        }

        TransferToAgribank transferToAgribank = transferToAgribankClient.findByStaffCode(transferToAgribankDraft.getStaffCode()).getData();
        mapDraftToOfficial(transferToAgribank, transferToAgribankDraft);

        DV dv = DV.builder()
                .staffCode(transferToAgribank.getStaffCode())
                .fullName(transferToAgribank.getFullName())
                .dvStatus(EDVStatus.PARTY_MEMBER.getStatus())
                .organizationCode(transferToAgribank.getReceivingOrgCCode())
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transferToAgribankDraft.setApprovedBy(userDetails.getId());
        transferToAgribankDraft.setStatus(EApprovalStatus.APPROVED.getId());

        DvOrgHistoryRequest historyRequest = DvOrgHistoryRequest.builder()
                .staffCode(transferToAgribankDraft.getStaffCode())
                .refId(transferToAgribank.getProcessId())
                .build();
        DvOrgHistory history = dvOrgHistoryClient.findByStaffCodeAndRefId(historyRequest).getData();

        if (history == null) {
            throw new CommonException("Không tìm thấy dữ liệu bản ghi!");
        }

        history.setEffectiveDate(transferToAgribankDraft.getEffectiveDate());
        history.setNewOrgCode(transferToAgribankDraft.getReceivingOrgCCode());

        ApproveTransferToRequest transferToRequest = ApproveTransferToRequest.builder()
                .transfer(transferToAgribank)
                .process(transferProcess)
                .dv(dv)
                .draft(transferToAgribankDraft)
                .history(history)
                .build();
        transferToAgribankClient.saveEntities(transferToRequest);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String draftId) {
        TransferToAgribankDraft transferToAgribankDraft = transferToDraftClient.findById(draftId).getData().orElse(null);
        if (Objects.isNull(transferToAgribankDraft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transferToAgribankDraft.setStatus(EApprovalStatus.DENIED.getId());
        transferToAgribankDraft.setApprovedBy(userDetails.getId());
        transferToDraftClient.save(transferToAgribankDraft);
    }

    private void mapDraftToOfficial(TransferToAgribank transfer, TransferToAgribankDraft draft) {
        transfer.setStaffCode(draft.getStaffCode());
        transfer.setFullName(draft.getFullName());
        transfer.setDecisionNumber(draft.getDecisionNumber());
        transfer.setIssueDate(draft.getIssueDate());
        transfer.setEffectiveDate(draft.getEffectiveDate());
        transfer.setIssuingOrganization(draft.getIssuingOrganization());
        transfer.setExpectedExpiryDate(draft.getExpectedExpiryDate());
        transfer.setFirstIntroNumber(draft.getFirstIntroNumber());
        transfer.setFirstIntroDate(draft.getFirstIntroDate());
        transfer.setTransferringPartyName(draft.getTransferringPartyName());
        transfer.setSecondIntroNumber(draft.getSecondIntroNumber());
        transfer.setTransferDate(draft.getTransferDate());
        transfer.setReceivingOrgBCode(draft.getReceivingOrgBCode());
        transfer.setReceivingOrgBName(draft.getReceivingOrgBName());
        transfer.setReceivingOrgCCode(draft.getReceivingOrgCCode());
        transfer.setReceivingOrgCName(draft.getReceivingOrgCName());
    }

    public PageResponse<DvRp28Response> searchRp28(SearchRpRequest request){
        return transferToAgribankClient.searchRp28(request).getData();
    }
}
