package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.report26.RemoveNamePartyClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.remove_name_party.RemoveNamePartyRequest;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.report26.RemoveNameParty;
import com.agribank.qldvutils.entity.report26.RemoveNamePartyDraft;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RemoveNamePartyService implements EntityHandler {
    private final RemoveNamePartyClient client;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;
    private final CommitteeDecisionService committeeDecisionService;
    private final RequestService requestService;
    private final RequestClient requestClient;
    private final Report26Service report26Service;
    private final UserService userService;
    private final EForm form = EForm.BIEU_26_REMOVE_NAME_PARTY;
    private final RemoveNamePartyDraftService removeNamePartyDraftService;
    private final DvHistoryService dvHistoryService;

    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(RemoveNamePartyDraft.FIELD_MAP);
    }

    public RemoveNamePartyDraft createDraft(RemoveNamePartyRequest request) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        UserDto dvInfo = userService.findByStaffCodeAndOrganizationCode(request.getStaffCode(), request.getOrganizationCode());
        if (Objects.isNull(dvInfo)) {
            throw new CommonException("Người dùng có mã nhân viên: " +
                    request.getStaffCode() +
                    " và mã tổ chức Đảng: " +
                    request.getOrganizationCode() +
                    " không chính xác. Vui lòng kiểm tra lại"
            );
        }
        UserDetailsImpl userRequested = userService.getUserRequested();

        CommitteeDecision committeeDecision = committeeDecisionService.findByCode(request.getCommitteeDecision());
        if (Objects.isNull(committeeDecision)){
            throw new CommonException("Sai code cấp ủy quyết định. Vui lòng kiểm tra lại");
        }

        RemoveNameParty removeNameParty = null;
        if (Objects.nonNull(request.getId())){
            removeNameParty = client.findById(request.getId()).getData()
                    .orElseThrow(()-> new CommonException("id request không chính xác"));
        }

        RemoveNamePartyDraft requestedDraft = modelMapper.map(request, RemoveNamePartyDraft.class);
        requestedDraft.setStatus(EApprovalStatus.PENDING.getId());
        requestedDraft.setUsernameCreated(userRequested.getUsername());
        requestedDraft.setDeleted(ERecordStatus.ACTIVE.getStatus());

        Request partActivityRequest = requestService.initializeRequest(requestedDraft, removeNameParty, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());

        if (Objects.nonNull(request.getId())){
            requestedDraft.setRefId(requestedDraft.getId());
        }
        requestedDraft = removeNamePartyDraftService.save(requestedDraft);

        partActivityRequest.setReferenceId(requestedDraft.getId());
        partActivityRequest.setStaffCode(requestedDraft.getStaffCode());
        partActivityRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return requestedDraft;
    }

    public String createRequestDelete(String id){
        RemoveNameParty removeNameParty = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        Report26 report26 = report26Service.findByRefId(id);
        if (Objects.isNull(report26)){
            throw new CommonException("Sai id, vui lòng kiểm tra lại");
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request partActivityRequest = requestService.initializeRequest(null, removeNameParty, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setReferenceId(id);
        partActivityRequest.setStaffCode(report26.getStaffCode());
        partActivityRequest.setOrganizationCode(report26.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return "Tạo yêu cầu thành công";
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        RemoveNamePartyDraft removeNamePartyDraft = removeNamePartyDraftService.findById(referenceId);

        removeNamePartyDraft.setStatus(EApprovalStatus.APPROVED.getId());
        removeNamePartyDraft.setUsernameAccepted(userDetails.getUsername());


        RemoveNameParty removeNameParty = modelMapper.map(removeNamePartyDraft, RemoveNameParty.class);

        removeNameParty = client.save(removeNameParty).getData();

        Report26 report26 = modelMapper.map(removeNamePartyDraft, Report26.class);
        report26.setRefId(removeNameParty.getId());
        report26.setType(EReport26.REMOVE_NAME_PARTY.getId());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        dvHistoryService.saveDV(removeNamePartyDraft.getStaffCode(),
                EDVStatus.REMOVE_NAME_PARTY.getStatus(),
                EDVStatus.REMOVE_NAME_PARTY.getName());
        removeNamePartyDraftService.save(removeNamePartyDraft);
        report26Service.save(report26);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        UserDetailsImpl userRequested = userService.getUserRequested();

        RemoveNamePartyDraft removeNamePartyDraft = removeNamePartyDraftService.findById(referenceId);
        removeNamePartyDraft.setStatus(EApprovalStatus.APPROVED.getId());
        removeNamePartyDraft.setUsernameAccepted(userRequested.getUsername());
        RemoveNameParty removeNameParty = modelMapper.map(removeNamePartyDraft, RemoveNameParty.class);
        removeNameParty.setId(removeNamePartyDraft.getRefId());

        Report26 report26 = report26Service.findByRefId(removeNamePartyDraft.getRefId());
        report26.setDecisionNumber(removeNamePartyDraft.getDecisionNumber());
        report26.setDecisionDate(removeNamePartyDraft.getDecisionDate());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());
        report26Service.save(report26);
        client.save(removeNameParty);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        Report26 report26 = report26Service.findByRefId(referenceId);
        report26.setDeleted(ERecordStatus.DELETED.getStatus());
        report26Service.save(report26);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        RemoveNamePartyDraft removeNamePartyDraft = removeNamePartyDraftService.findById(referenceId);
        removeNamePartyDraft.setStatus(EApprovalStatus.DENIED.getId());
        removeNamePartyDraftService.save(removeNamePartyDraft);
    }

    public RP26DetailResponse getDetail(String id){
        return client.getDetail(id).getData();
    }
}
