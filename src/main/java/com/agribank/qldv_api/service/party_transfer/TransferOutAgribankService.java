package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_out.TransferOutAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_out.TransferOutAgribankDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferOutAgribankRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribankDraft;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribankDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
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
public class TransferOutAgribankService implements EntityHandler {
    ModelMapper modelMapper;

    TransferProcessClient transferProcessClient;
    TransferOutAgribankClient outAgribankClient;
    TransferOutAgribankDraftClient draftOutClient;
    RequestClient requestClient;
    DVClient dvClient;

    RequestService requestService;

    EForm form = EForm.BIEU_25_TRANSFER_OUT_AGRIBANK;

    public Request create(TransferOutAgribankRequest request) {
        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();

        if (Objects.isNull(dv)) {
            throw new CommonException("Không tìm thấy thông tin đảng viên");
        }

        List<TransferOutAgribankDraft> draftList = draftOutClient.findByStaffCode(request.getStaffCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu chuyển sinh hoạt đảng cho cán bộ");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferOutAgribankDraft draft = modelMapper.map(request, TransferOutAgribankDraft.class);
        draft.setFullName(dv.getFullName());
        draft.setCreatedBy(userDetails.getId());
        draft = draftOutClient.save(draft).getData();

        Request transferOutAgribankRequest = requestService.initializeRequest(draft, null, form, TransferOutAgribankDraft.FIELD_MAP);
        transferOutAgribankRequest.setOrganizationCode(dv.getOrganizationCode());
        transferOutAgribankRequest.setStaffCode(request.getStaffCode());
        transferOutAgribankRequest.setCreatedBy(userDetails.getId());
        transferOutAgribankRequest.setReferenceId(draft.getId());
        requestClient.save(transferOutAgribankRequest);

        return transferOutAgribankRequest;
    }

    public PageResponse<TransferOutAgribank> getList(TransferToFilterRequest request) {
        return outAgribankClient.getList(request).getData();
    }

    public TransferOutAgribank getDetail(String id) {
        TransferOutAgribank transfer = outAgribankClient.findById(id).getData().orElse(null);

        if (Objects.isNull(transfer)) {
            throw new CommonException(ExceptionMessage.NO_DATA);
        }

        return transfer;
    }

    public Request update(TransferOutAgribankRequest request) {
        TransferOutAgribank transfer = outAgribankClient.findById(request.getId()).getData().orElse(null);

        if (Objects.isNull(transfer)) {
            throw new CommonException(ExceptionMessage.NO_DATA);
        }

        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();

        if (Objects.isNull(dv)) {
            throw new CommonException("Không tìm thấy thông tin đảng viên");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferOutAgribankDraft draft = modelMapper.map(request, TransferOutAgribankDraft.class);
        draft.setId(null);
        draft.setFullName(dv.getFullName());
        draft.setCreatedBy(userDetails.getId());
        draft = draftOutClient.save(draft).getData();

        Request transferOutAgribankRequest = requestService.initializeRequest(draft, transfer, form, TransferOutAgribankDraft.FIELD_MAP);
        transferOutAgribankRequest.setOrganizationCode(dv.getOrganizationCode());
        transferOutAgribankRequest.setStaffCode(request.getStaffCode());
        transferOutAgribankRequest.setCreatedBy(userDetails.getId());
        transferOutAgribankRequest.setReferenceId(draft.getId());
        requestClient.save(transferOutAgribankRequest);

        return transferOutAgribankRequest;
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransferOutAgribankDraft draft = draftOutClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return false;
        }

        TransferProcess process = TransferProcess.builder()
                .staffCode(draft.getStaffCode())
                .fullName(draft.getFullName())
                .transferType(ETransferType.TRANSFER_OUT_AGRIBANK.getId())
                .organizationCode(Constants.BTCDU_CODE)
                .status(EProcessStatus.PROCESSING.getId())
                .build();
        process = transferProcessClient.save(process).getData();

        TransferOutAgribank transfer = modelMapper.map(draft, TransferOutAgribank.class);
        transfer.setProcessId(process.getId());
        outAgribankClient.save(transfer);

        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draftOutClient.save(draft);

        DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
        dv.setDvStatus(EDVStatus.WAITING_FOR_PARTY_ACTIVITIES_TRANSFER.getStatus());
        dvClient.save(dv);

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        TransferOutAgribankDraft draft = draftOutClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return false;
        }

        TransferProcess transferProcess = null;
        List<TransferProcess> transferProcesses = transferProcessClient.findProcessingTransfer(draft.getStaffCode(), ETransferType.TRANSFER_OUT_AGRIBANK.getId())
                .getData();

        if (
                !transferProcesses.isEmpty() &&
                (
                        Objects.nonNull(draft.getIntroDocumentNumber()) &&
                        Objects.nonNull(draft.getTransferDate()) &&
                        Objects.nonNull(draft.getReceivedOrganization())
                )
        ) {
            DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
            dv.setDvStatus(EDVStatus.OUTSIDE_AGRIBANK.getStatus());
            dvClient.save(dv);

            transferProcess = transferProcesses.get(0);
            transferProcess.setStatus(EProcessStatus.DONE.getId());
            transferProcessClient.save(transferProcess);
        }

        TransferOutAgribank transfer = outAgribankClient.findByStaffCode(draft.getStaffCode()).getData();
        mapDraftToOfficial(transfer, draft);
        outAgribankClient.save(transfer);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draftOutClient.save(draft);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {
        TransferOutAgribankDraft draft = draftOutClient.findById(referenceId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        draft.setStatus(EApprovalStatus.DENIED.getId());
        draft.setApprovedBy(userDetails.getId());
        draftOutClient.save(draft);
    }

    private void mapDraftToOfficial(TransferOutAgribank transfer, TransferOutAgribankDraft draft) {
        transfer.setStaffCode(draft.getStaffCode());
        transfer.setFullName(draft.getFullName());
        transfer.setDecisionNumber(draft.getDecisionNumber());
        transfer.setIssueDate(draft.getIssueDate());
        transfer.setEffectiveDate(draft.getEffectiveDate());
        transfer.setReason(draft.getReason());
        transfer.setIssuingOrganization(draft.getIssuingOrganization());
        transfer.setOrgCProposeDate(draft.getOrgCProposeDate());
        transfer.setOrgCProposeNumber(draft.getOrgCProposeNumber());
        transfer.setOrgBProposeDate(draft.getOrgBProposeDate());
        transfer.setOrgBProposeNumber(draft.getOrgBProposeNumber());
        transfer.setExpectedExpiryDate(draft.getExpectedExpiryDate());
        transfer.setIntroDocumentNumber(draft.getIntroDocumentNumber());
        transfer.setTransferDate(draft.getTransferDate());
        transfer.setReceivedOrganization(draft.getReceivedOrganization());
    }
}
