package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EProcessStatus;
import com.agribank.qldv_api.enums.ETransferType;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferToAgribankRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribankDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
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


    public List<TransferToAgribank> getList(TransferToFilterRequest request) {
        return transferToAgribankClient.getList(request).getData();
    }


    public TransferToAgribank getDetail(String id) {
        return transferToAgribankClient.findById(id).getData().orElse(null);
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
        transferToAgribankClient.save(transferToAgribank);

        transferToAgribankDraft.setApprovedBy(userDetails.getId());
        transferToAgribankDraft.setStatus(EApprovalStatus.APPROVED.getId());
        transferToDraftClient.save(transferToAgribankDraft).getData();

        return true;
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
}
