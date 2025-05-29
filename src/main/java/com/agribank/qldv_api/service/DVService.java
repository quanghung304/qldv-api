package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.gateway.DVDraftClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.dv.DVRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.dv.DVResponse;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.log.DVLogService;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvDraft;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DVService implements EntityHandler {
    private final DVClient dvClient;
    private final DVDraftClient dvDraftClient;
    private final OrganizationClient organizationClient;
    private final RequestClient requestClient;
    private final DVLogService dvLogService;
    private final CheckAuthorityService authorityService;
    private final RequestService requestService;
    private final ModelMapper modelMapper;

    private static final EForm form = EForm.BIEU_15;

    public PageResponse<DVResponse> search(SearchDVRequest request){
        PageResponse<DV> dvPageResponse = dvClient.search(request).getData();
        PageResponse<DVResponse> response = new PageResponse<>();
        if (Objects.isNull(dvPageResponse)) {
            return response;
        }

        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalItems(dvPageResponse.getTotalItems());

        if (Objects.nonNull(dvPageResponse.getData())) {
            response.setData(dvPageResponse.getData().stream()
                    .map(dv -> modelMapper.map(dv, DVResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public String create(List<DVRequest> requests) {
        List<DV> dvs = requests.stream().map(dv -> modelMapper.map(dv, DV.class)).toList();
        DefaultResponse<List<DV>> response = dvClient.saveAll(dvs);

        dvLogService.writeLogRegister(dvs);
        return response.getMessage();
    }

    public DV findById(String id) {
        return dvClient.findById(id).getData();
    }

    public DvDraft create(DVRequest dvRequest) {
        authorityService.hasAuthorityOverOrganization(dvRequest.getOrganizationCode());

        Organization organization = organizationClient.findByCode(dvRequest.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DvDraft dvDraft = modelMapper.map(dvRequest, DvDraft.class);
        dvDraft.setStatus(EApprovalStatus.PENDING.getId());
        dvDraft.setCreatedBy(userDetails.getId());
        dvDraft = dvDraftClient.save(dvDraft).getData();

        Request request = requestService.initializeRequest(dvDraft, null, form, DV.FIELD_MAP);
        request.setOrganizationCode(dvRequest.getOrganizationCode());
        request.setReferenceId(dvDraft.getId());
        request.setCreatedBy(userDetails.getId());
        requestClient.save(request);

        return dvDraft;
    }

    public DvDraft update(DVRequest dvRequest) {
        authorityService.hasAuthorityOverOrganization(dvRequest.getOrganizationCode());

        Organization organization = organizationClient.findByCode(dvRequest.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DV oldDv = dvClient.findById(dvRequest.getId()).getData();

        if (Objects.isNull(oldDv)) {
            throw new CommonException("KOong tim thay thong tin dang vien");
        }

        DvDraft newDV = modelMapper.map(dvRequest, DvDraft.class);
        newDV.setStatus(EApprovalStatus.PENDING.getId());
        newDV.setCreatedBy(userDetails.getId());
        newDV = dvDraftClient.save(newDV).getData();

        Request request = requestService.initializeRequest(newDV, oldDv, form, DV.FIELD_MAP);
        request.setOrganizationCode(dvRequest.getOrganizationCode());
        request.setReferenceId(newDV.getId());
        request.setCreatedBy(userDetails.getId());
        requestClient.save(request);

        return newDV;

    }

    @Override
    public boolean applyCreate(String dvId, UserDetailsImpl userDetails) {
        DvDraft dvDraft = dvDraftClient.findById(dvId).getData()
                .orElse(null);

        if (Objects.isNull(dvDraft) || Objects.equals(dvDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            return false;
        }

        dvDraft.setStatus(EApprovalStatus.APPROVED.getId());
        dvDraft.setApprovedBy(userDetails.getId());

        return false;
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
    public void setDenied(String referenceId) {

    }
}
