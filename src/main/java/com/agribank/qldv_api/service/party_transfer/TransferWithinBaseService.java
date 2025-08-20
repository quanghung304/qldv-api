package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_base.TransferWithinBaseClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_base.TransferWithinBaseDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_transfer.TransferWithinBaseRequest;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.Report31Dto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBase;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBaseDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.dv_org.DvOrgHistoryRequest;
import com.agribank.qldvutils.request.party_transfer.ApproveTransferBaseRequest;
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

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;


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
    DvOrgHistoryClient dvOrgHistoryClient;

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

        DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
        TransferWithinBase transferWithinBase = modelMapper.map(draft, TransferWithinBase.class);
        transferWithinBase.setOldOrganizationCode(dv.getOrganizationCode());
        transferWithinBase.setCreatedBy(draft.getCreatedBy());
        transferWithinBase.setApprovedBy(userDetails.getId());
        transferWithinBase = transferWithinBaseClient.save(transferWithinBase).getData();

        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());

        dv.setOrganizationCode(draft.getOrganizationCode());

        DvOrgHistory history = DvOrgHistory.builder()
                .staffCode(draft.getStaffCode())
                .oldOrgCode(dv.getOrganizationCode())
                .newOrgCode(draft.getOrganizationCode())
                .refId(transferWithinBase.getId())
                .effectiveDate(draft.getEffectiveDate())
                .action(form.getCode())
                .build();

        ApproveTransferBaseRequest transferBaseRequest = ApproveTransferBaseRequest.builder()
                .transfer(null)
                .dv(dv)
                .draft(draft)
                .history(history)
                .build();
        transferWithinBaseClient.saveEntities(transferBaseRequest);

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

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        draft.setApprovedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.APPROVED.getId());

        DV dv = dvClient.findByStaffCode(draft.getStaffCode()).getData();
        dv.setOrganizationCode(draft.getOrganizationCode());

        DvOrgHistoryRequest historyRequest = DvOrgHistoryRequest.builder()
                .staffCode(draft.getStaffCode())
                .refId(transfer.getId())
                .build();
        DvOrgHistory history = dvOrgHistoryClient.findByStaffCodeAndRefId(historyRequest).getData();

        if (history == null) {
            throw new CommonException("Không tìm thấy dữ liệu bản ghi!");
        }

        history.setEffectiveDate(draft.getEffectiveDate());
        history.setOldOrgCode(dv.getOrganizationCode());
        history.setNewOrgCode(draft.getOrganizationCode());

        ApproveTransferBaseRequest transferBaseRequest = ApproveTransferBaseRequest.builder()
                .transfer(transfer)
                .dv(dv)
                .draft(draft)
                .history(history)
                .build();
        transferWithinBaseClient.saveEntities(transferBaseRequest);

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

    public PageResponse<Report31Dto> search31(SearchRpRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageResponse<Report31Dto> response = transferWithinBaseClient.search31(request).getData();
        if (Objects.nonNull(userDetails.getOrganizationCode()) && userDetails.getOrganizationCode().equals(String.valueOf(BRANCH_CODE_HEAD_QUARTER))) {
            return response;
        }
        List<Report31Dto> report31Dtos = response.getData();
        List<Report31Dto> newReport31Dtos = report31Dtos.stream().map((x) -> {
            x.setController(null);
            x.setImplementationStaff(null);
            return x;
        }).toList();
        response.setData(newReport31Dtos);
        return response;
    }

    public TransferWithinBaseDraft getDraft(String id) {
        return draftClient.findById(id).getData().orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu chuyển sinh hoạt đảng."));
    }

    public TransferWithinBaseDraft updateDraft(TransferWithinBaseRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không được để trống trường id");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Request requestDetail = requestClient.findByReferenceId(request.getId()).getData();

        if (Objects.isNull(requestDetail)) {
            throw new CommonException("Yêu cầu phê duyệt không tồn tại!");
        }

        if (requestDetail.getStatus() != EApprovalStatus.PENDING.getId()) {
            throw new CommonException("Yêu cầu đã được phê duyệt hoặc bị từ chối!");
        }

        Organization organization = organizationClient.findByCode(request.getOrganizationCode()).getData();

        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        if (!organization.getCode().contains(userDetails.getOrganizationCode())) {
            throw new CommonException("Chi bộ chuyển đến không trực thuộc đảng bộ cơ sở");
        }

        DV dv = dvClient.findByStaffCode(request.getStaffCode()).getData();

        TransferWithinBaseDraft draft = draftClient.findById(request.getId()).getData().orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu chuyển sinh hoạt đảng."));
        draft.setStaffCode(request.getStaffCode());
        draft.setFullName(dv.getFullName());
        draft.setDecisionNumber(request.getDecisionNumber());
        draft.setIssueDate(request.getIssueDate());
        draft.setEffectiveDate(request.getEffectiveDate());
        draft.setIssuingOrganization(request.getIssuingOrganization());
        draft.setIntroDocumentNumber(request.getIntroDocumentNumber());
        draft.setIntroDocumentDate(request.getIntroDocumentDate());
        draft.setTransferDate(request.getTransferDate());
        draft.setOrganizationCode(request.getOrganizationCode());
        draft.setOrganizationName(organization.getName());

        requestDetail.setNewData(requestService.createJsonData(draft, TransferWithinBaseDraft.FIELD_MAP));
        requestClient.save(requestDetail);

        return draftClient.save(draft).getData();
    }
}
