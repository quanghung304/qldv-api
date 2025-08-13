package com.agribank.qldv_api.service.form02;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.gateway.form02.rename.OrganizationRenameClient;
import com.agribank.qldv_api.gateway.form02.rename.OrganizationRenameDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.form02.OrganizationRenameRequest;
import com.agribank.qldv_api.response.form02.OrganizationRenameResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.BaseFormEntity;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRename;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRenameDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.form02.*;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class OrganizationRenameService implements EntityHandler {
    private final OrganizationClient organizationClient;
    private final RequestClient requestClient;
    private final OrganizationHistoryClient historyClient;
    private final OrganizationRenameDraftClient organizationRenameDraftClient;
    private final OrganizationRenameClient organizationRenameClient;
    private final CheckAuthorityService checkAuthorityService;
    private final OrganizationService organizationService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    private void validateRequest(OrganizationRenameRequest request, Organization organization) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        List<OrganizationRenameDraft> draftList = organizationRenameDraftClient.findPendingDraftByCode(request.getOrganizationCode()).getData();

        if (!draftList.isEmpty()) {
            throw new CommonException("Đã tồn tại yêu cầu đổi tên cho tổ chức đảng này");
        }
    }

    public Map<String, String> getCombinedFieldMap() {
        Map<String, String> combinedFieldMap = new LinkedHashMap<>();
        combinedFieldMap.putAll(BaseFormEntity.BASE_FIELD_MAP);
        combinedFieldMap.putAll(OrganizationRenameDraft.FIELD_MAP);

        return combinedFieldMap;
    }

    public OrganizationRenameDraft createRenameRequest(OrganizationRenameRequest request) {
        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)) {
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        validateRequest(request, organization);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationRenameDraft draft = new OrganizationRenameDraft();
        draft.setOrganizationCode(request.getOrganizationCode());
        draft.setOldOrganizationName(organization.getName());
        draft.setOrganizationName(request.getOrganizationName());
        draft.setDecisionCommittee(request.getDecisionCommittee());
        draft.setConclusionNumber(request.getConclusionNumber());
        draft.setConclusionDate(request.getConclusionDate());
        draft.setDecisionNumber(request.getDecisionNumber());
        draft.setDecisionDate(request.getDecisionDate());
        draft.setEffectiveDate(request.getEffectiveDate());
        draft.setCreatedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());

        draft = organizationRenameDraftClient.save(draft).getData();

        Request renameRequest = requestService.initializeRequest(draft, null, EForm.BIEU_02_RENAME, getCombinedFieldMap());
        renameRequest.setOrganizationCode(renameRequest.getOrganizationCode());
        renameRequest.setReferenceId(draft.getId());
        renameRequest.setCreatedBy(userDetails.getId());
        requestClient.save(renameRequest);

        return draft;
    }

    public OrganizationRenameDraft updateRenameRequest(OrganizationRenameRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("Không được để trống trường id");
        }

        OrganizationRename organizationRename = organizationRenameClient.findById(request.getId()).getData().orElseThrow(
                () -> new CommonException("Không tồn tại yêu cầu đổi tên")
        );

        Organization organization = organizationService.findByCode(request.getOrganizationCode());

        if (Objects.isNull(organization)) {
            throw new CommonException("Không tồn tại TCD có mã: " + request.getOrganizationCode());
        }

        validateRequest(request, organization);

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationRenameDraft draft = modelMapper.map(request, OrganizationRenameDraft.class);
        draft.setId(null);
        draft.setCreatedBy(userDetails.getId());
        draft.setStatus(EApprovalStatus.PENDING.getId());
        draft.setRefId(organizationRename.getId());
        draft = organizationRenameDraftClient.save(draft).getData();

        Request organizationRenameRequest = requestService.initializeRequest(draft, organizationRename, EForm.BIEU_02_RENAME, getCombinedFieldMap());
        organizationRenameRequest.setOrganizationCode(request.getOrganizationCode());
        organizationRenameRequest.setReferenceId(draft.getId());
        organizationRenameRequest.setCreatedBy(userDetails.getId());
        requestClient.save(organizationRenameRequest);

        return draft;
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        OrganizationRenameDraft draft = organizationRenameDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu đổi tên."));

        OrganizationRename organizationRename = modelMapper.map(draft, OrganizationRename.class);

        return approveDraftRequest(draft, organizationRename);
    }

    @Override
    public boolean applyUpdate(String draftId) {
        OrganizationRenameDraft draft = organizationRenameDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu đổi tên."));

        OrganizationRename organizationRename = organizationRenameClient.findById(draft.getRefId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu đổi tên."));

        organizationRename = modelMapper.map(draft, OrganizationRename.class);
        organizationRename.setId(draft.getRefId());

        return approveDraftRequest(draft, organizationRename);
    }

    private boolean approveDraftRequest(OrganizationRenameDraft draft, OrganizationRename organizationRename) {
        Organization organization = organizationClient.findByCode(organizationRename.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) return false;

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        organization.setName(draft.getOrganizationName());
        draft.setStatus(EApprovalStatus.APPROVED.getId());
        draft.setApprovedBy(userDetails.getId());

        OrganizationHistory history = null;

        if (Objects.nonNull(organizationRename.getId())) {
            history = historyClient.findByRefId(EReport01Type.RENAME.getId(), organizationRename.getId()).getData();
        }

        if (Objects.isNull(history)) {
            history = OrganizationHistory.builder()
                    .name(organizationRename.getOldOrganizationName())
                    .type(EReport01Type.RENAME.getId())
                    .refId(organizationRename.getId())
                    .effectiveDate(organizationRename.getEffectiveDate())
                    .build();
            history.setCode(organizationRename.getOrganizationCode());
        } else {
            history.setCode(organizationRename.getOrganizationCode());
            history.setName(organizationRename.getOldOrganizationName());
            history.setEffectiveDate(organizationRename.getEffectiveDate());
        }

        OrganizationRenameMetaRequest request = OrganizationRenameMetaRequest.builder()
                .organization(organization)
                .data(organizationRename)
                .draft(draft)
                .history(history)
                .build();

        organizationRenameClient.saveEntities(request);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return true;
    }

    @Override
    public void setDenied(String draftId) {
        OrganizationRenameDraft draft = organizationRenameDraftClient.findById(draftId).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy yêu cầu đổi tên."));
        draft.setStatus(EApprovalStatus.DENIED.getId());
        organizationRenameDraftClient.save(draft);
    }

    public PageResponse<OrganizationRename> getList(OrganizationRenameFilterRequest request) {
        if (Objects.isNull(request.getOrganizationCode())) {
            request.setOrganizationCode(CommonUtils.getOrganizationByRequestedUser());
        }

        Page<OrganizationRename> organizationRenames = organizationRenameClient.getList(request).getData();

        PageResponse<OrganizationRename> response = new PageResponse<>();
        response.setData(organizationRenames.getContent());
        response.setCurrentPage(organizationRenames.getNumber());
        response.setTotalPages(organizationRenames.getTotalPages());
        response.setTotalItems(organizationRenames.getTotalElements());

        return response;
    }

    public OrganizationRename getDetail(String id) {
        return organizationRenameClient.findById(id).getData().orElse(null);
    }

    public OrganizationRenameResponse getDraftDetail(String id) {
        OrganizationRenameDraft organizationRenameDraft = organizationRenameDraftClient.findById(id).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        return modelMapper.map(organizationRenameDraft, OrganizationRenameResponse.class);
    }

    @SneakyThrows
    public OrganizationRenameResponse updateDraft(OrganizationRenameRequest request) {
        if (Objects.isNull(request.getId())) {
            throw new CommonException("id is required");
        }

        OrganizationRenameDraft organizationRenameDraft = organizationRenameDraftClient.findById(request.getId())
                .getData().orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        Request organizationRenameRequest = requestClient.findByReferenceId(request.getId()).getData();
        if (Objects.isNull(organizationRenameRequest)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        if (EApprovalStatus.PENDING.getId() != organizationRenameRequest.getStatus()) {
            throw new CommonException("Yêu cầu này đã được duyệt, vui lòng tạo request khác");
        }
        UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        organizationRenameDraft.setOrganizationCode(request.getOrganizationCode());
        organizationRenameDraft.setOrganizationName(request.getOrganizationName());
        organizationRenameDraft.setOldOrganizationName(request.getOldOrganizationName());

        Map<String, Object> newDataMap = CommonUtils.createFilteredDataMap(organizationRenameDraft, getCombinedFieldMap());
        organizationRenameRequest.setNewData(objectMapper.writeValueAsString(newDataMap));
        organizationRenameRequest.setCreatedBy(userRequested.getId());

        organizationRenameDraftClient.save(organizationRenameDraft);
        requestClient.save(organizationRenameRequest);
        return modelMapper.map(organizationRenameDraft, OrganizationRenameResponse.class);
    }
}
