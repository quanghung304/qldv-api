package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EDVStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_base.TransferWithinBaseClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_base.TransferWithinBaseDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferWithinBaseRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBase;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBaseDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferWithinBaseService implements EntityHandler {
    ModelMapper modelMapper;

    TransferWithinBaseClient transferWithinBaseClient;
    TransferWithinBaseDraftClient draftClient;
    RequestClient requestClient;
    DVClient dvClient;
    OrganizationClient organizationClient;

    RequestService requestService;

    EForm form = EForm.BIEU_25_TRANSFER_WITHIN_BASE;

    public Request createOrUpdate(TransferWithinBaseRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();

        if (Objects.isNull(dv)) {
            throw new CommonException(ExceptionMessage.MEMBER_NOT_FOUND);
        }

        if (!dv.getOrganizationCode().contains(userDetails.getOrganizationCode())) {
            throw new CommonException("Đảng viên không thuộc đảng bộ cơ sở");
        }

        TransferWithinBaseDraft draft = draftClient.findByStaffCode(request.getStaffCode()).getData();

        if (Objects.nonNull(draft)) {
            throw new CommonException("Đã tồn tại yêu cầu chuyển sinh hoạt đảng cho cán bộ");
        }

        Organization organization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        if (!organization.getCode().contains(userDetails.getOrganizationCode())) {
            throw new CommonException("Chi bộ chuyển đến không trực thuộc đảng bộ cơ sở");
        }

        TransferWithinBase oldTransfer = Objects.nonNull(request.getId()) ? transferWithinBaseClient.findById(request.getId()).getData().orElse(null) : null;

        draft = modelMapper.map(request, TransferWithinBaseDraft.class);
        draft.setFullName(dv.getFullName());
        draft.setOrganizationName(organization.getName());
        draft.setCreatedBy(userDetails.getId());

        if (Objects.nonNull(oldTransfer)) {
            draft.setReferenceId(oldTransfer.getId());
        }

        draft = draftClient.save(draft).getData();

        Request transferRequest = requestService.initializeRequest(draft, oldTransfer, form, TransferWithinBaseDraft.FIELD_MAP);
        transferRequest.setOrganizationCode(dv.getOrganizationCode());
        transferRequest.setStaffCode(request.getStaffCode());
        transferRequest.setCreatedBy(userDetails.getId());
        transferRequest.setReferenceId(draft.getId());
        requestClient.save(transferRequest);

        return transferRequest;
    }

    public PageResponse<TransferWithinBase> getList(TransferToFilterRequest request) {
        return transferWithinBaseClient.getList(request).getData();
    }

    public TransferWithinBase getDetail(String id) {
        return transferWithinBaseClient.findById(id).getData().orElse(null);
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        TransferWithinBaseDraft draft = draftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return false;
        }

        TransferWithinBase transferWithinBase = modelMapper.map(draft, TransferWithinBase.class);
        transferWithinBaseClient.save(transferWithinBase);

        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draftClient.save(draft);

        DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
        dv.setOrganizationCode(draft.getOrganizationCode());
        dvClient.save(dv);

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        TransferWithinBaseDraft draft = draftClient.findById(draftId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return false;
        }

        TransferWithinBase transfer = transferWithinBaseClient.findById(draft.getReferenceId()).getData().orElse(null);

        if (Objects.isNull(transfer)) {
            return false;
        }

        mapDraftToOfficial(draft, transfer);
        transferWithinBaseClient.save(transfer);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draftClient.save(draft);

        DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
        dv.setOrganizationCode(draft.getOrganizationCode());
        dvClient.save(dv);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String referenceId) {
        TransferWithinBaseDraft draft = draftClient.findById(referenceId).getData().orElse(null);

        if (Objects.isNull(draft)) {
            return;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        draft.setStatus(EApprovalStatus.DENIED.getId());
        draft.setApprovedBy(userDetails.getId());
        draftClient.save(draft);
    }

    private void mapDraftToOfficial(TransferWithinBaseDraft draft, TransferWithinBase transfer) {
        transfer.setStaffCode(draft.getStaffCode());
        transfer.setFullName(draft.getFullName());
        transfer.setDecisionNumber(draft.getDecisionNumber());
        transfer.setIssueDate(draft.getIssueDate());
        transfer.setEffectiveDate(draft.getEffectiveDate());
        transfer.setIssuingOrganization(draft.getIssuingOrganization());
        transfer.setIntroDocumentNumber(draft.getIntroDocumentNumber());
        transfer.setIntroDocumentDate(draft.getIntroDocumentDate());
        transfer.setTransferDate(draft.getTransferDate());
        transfer.setOrganizationCode(draft.getOrganizationCode());
        transfer.setOrganizationName(draft.getOrganizationName());
    }
}
