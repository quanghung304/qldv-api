package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_temporary.TransferTemporaryClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_temporary.TransferTemporaryDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferTemporaryRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.TransferTemporaryDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.entity.party_transfer.transfer_temporary.TransferTemporary;
import com.agribank.qldvutils.entity.party_transfer.transfer_temporary.TransferTemporaryDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferTemporaryFilterRequest;
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
public class TransferTemporaryService implements EntityHandler {
    ModelMapper modelMapper;

    OrganizationClient organizationClient;
    TransferTemporaryClient transferTemporaryClient;
    TransferTemporaryDraftClient transferTemporaryDraftClient;
    TransferProcessClient transferProcessClient;
    RequestClient requestClient;
    DVClient dvClient;

    RequestService requestService;

    EForm form = EForm.BIEU_25_TRANSFER_TEMPORARY;

    public Request createRequest(TransferTemporaryRequest request) {
        List<TransferTemporaryDraft> drafts = transferTemporaryDraftClient.findByStaffCode(request.getStaffCode()).getData();

        if (!drafts.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu chuyển sinh hoạt đảng cho cán bộ");
        }

        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();

        if (Objects.isNull(dv)){
            throw new CommonException("Không tồn tại đảng viên");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferTemporaryDraft transferTemporaryDraft = modelMapper.map(request, TransferTemporaryDraft.class);
        transferTemporaryDraft.setCreatedBy(userDetails.getId());
        transferTemporaryDraft = transferTemporaryDraftClient.save(transferTemporaryDraft).getData();

        Request transferTemporaryRequest = requestService.initializeRequest(transferTemporaryDraft, null, form, TransferTemporaryDraft.FIELD_MAP);
        transferTemporaryRequest.setOrganizationCode(dv.getOrganizationCode());
        transferTemporaryRequest.setStaffCode(request.getStaffCode());
        transferTemporaryRequest.setCreatedBy(userDetails.getId());
        transferTemporaryRequest.setReferenceId(transferTemporaryDraft.getId());
        requestClient.save(transferTemporaryRequest);

        return transferTemporaryRequest;
    }


    public PageResponse<TransferTemporaryDto> getList(TransferTemporaryFilterRequest request) {
        return transferTemporaryClient.getList(request).getData();
    }


    public TransferTemporary getDetail(String id) {
        return transferTemporaryClient.findById(id).getData().orElse(null);
    }

    public Request update(TransferTemporaryRequest request) {
        List<TransferTemporaryDraft> drafts = transferTemporaryDraftClient.findByStaffCode(request.getStaffCode()).getData();

        if (!drafts.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu chuyển sinh hoạt đảng cho cán bộ");
        }

        if (
                (Objects.isNull(request.getReceivingOrgCode()) && Objects.isNull(request.getReceivingOutOrgName())) ||
                        (Objects.nonNull(request.getReceivingOrgCode()) && Objects.nonNull(request.getReceivingOutOrgName()))
        ) {
            throw new CommonException("Tổ chức chuyển sinh hoạt đảng không chính xác");
        }

        Organization getReceivingOrg = null;
        if (Objects.nonNull(request.getReceivingOrgCode())){
            getReceivingOrg = organizationClient.findByCode(request.getReceivingOrgCode()).getData();
            if (Objects.isNull(getReceivingOrg)) {
                throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
            }
        }

        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();
        if (Objects.isNull(dv)){
            throw new CommonException("Không tồn tại đảng viên");
        }

        TransferTemporary transferTemporary = transferTemporaryClient.findById(request.getId()).getData().orElse(null);

        if (Objects.isNull(transferTemporary)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TransferTemporaryDraft transferTemporaryDraft = modelMapper.map(request, TransferTemporaryDraft.class);
        transferTemporaryDraft.setId(null);

        if (Objects.nonNull(getReceivingOrg)) {
            transferTemporaryDraft.setReceivingOrgName(getReceivingOrg.getName());
        }
        transferTemporaryDraft.setCreatedBy(userDetails.getId());
        transferTemporaryDraft.setRefId(transferTemporary.getId());
        transferTemporaryDraft = transferTemporaryDraftClient.save(transferTemporaryDraft).getData();

        Request transferTemporaryRequest = requestService.initializeRequest(transferTemporaryDraft, transferTemporary, form, TransferTemporaryDraft.FIELD_MAP);
        transferTemporaryRequest.setOrganizationCode(dv.getOrganizationCode());
        transferTemporaryRequest.setStaffCode(request.getStaffCode());
        transferTemporaryRequest.setCreatedBy(userDetails.getId());
        transferTemporaryRequest.setReferenceId(transferTemporaryDraft.getId());
        requestClient.save(transferTemporaryRequest);

        return transferTemporaryRequest;
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransferTemporaryDraft transferTemporaryDraft = transferTemporaryDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(transferTemporaryDraft)) {
            return false;
        }

        TransferProcess transferProcess = TransferProcess.builder()
                .staffCode(transferTemporaryDraft.getStaffCode())
                .fullName(transferTemporaryDraft.getFullName())
                .transferType(ETransferType.TEMPORARY_TRANSFER.getId())
                .organizationCode(Constants.BTCDU_CODE)
                .status(EProcessStatus.PROCESSING.getId())
                .build();
        transferProcess = transferProcessClient.save(transferProcess).getData();

        TransferTemporary transferTemporary = modelMapper.map(transferTemporaryDraft, TransferTemporary.class);
        transferTemporary.setProcessId(transferProcess.getId());

        transferTemporary = transferTemporaryClient.save(transferTemporary).getData();
        transferTemporaryDraft.setRefId(transferTemporary.getId());
        transferTemporaryDraft.setApprovedBy(userDetails.getId());
        transferTemporaryDraft.setStatus(EApprovalStatus.APPROVED.getId());
        transferTemporaryDraftClient.save(transferTemporaryDraft).getData();

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        TransferTemporaryDraft transferTemporaryDraft = transferTemporaryDraftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(transferTemporaryDraft)) {
            return false;
        }

        DV dv = dvClient.findByStaffCode(transferTemporaryDraft.getStaffCode()).getData();
        if (Objects.isNull(dv)) {
            return false;
        }

        TransferProcess transferProcess = null;
        List<TransferProcess> transferProcesses = transferProcessClient.findProcessingTransfer(transferTemporaryDraft.getStaffCode(), ETransferType.TEMPORARY_TRANSFER.getId())
                .getData();

        if (!transferProcesses.isEmpty()) {
            transferProcess = transferProcesses.get(0);
            transferProcess.setStatus(EProcessStatus.DONE.getId());
            transferProcessClient.save(transferProcess);
        }

        TransferTemporary transferTemporary = transferTemporaryClient
                .findById(transferTemporaryDraft.getRefId())
                .getData()
                .orElseThrow(()-> new CommonException("Không tìm thấy yêu cầu chuyển sinh hoạt đảng tạm thời"));
        String transferTemporaryId = transferTemporary.getId();
        modelMapper.map(transferTemporaryDraft, transferTemporary);
        transferTemporary.setId(transferTemporaryId);
        transferTemporaryClient.save(transferTemporary);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transferTemporaryDraft.setApprovedBy(userDetails.getId());
        transferTemporaryDraft.setStatus(EApprovalStatus.APPROVED.getId());
        transferTemporaryDraftClient.save(transferTemporaryDraft).getData();

        dv.setDvStatus(EDVStatus.TEMPORARY_PARTY_ACTIVITIES.getStatus());
        dvClient.save(dv);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String draftId) {
        TransferTemporaryDraft transferTemporaryDraft = transferTemporaryDraftClient.findById(draftId).getData().orElse(null);
        if (Objects.isNull(transferTemporaryDraft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transferTemporaryDraft.setStatus(EApprovalStatus.DENIED.getId());
        transferTemporaryDraft.setApprovedBy(userDetails.getId());
        transferTemporaryDraftClient.save(transferTemporaryDraft);
    }

}
