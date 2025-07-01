package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.report26.LeavePartyClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.leave_party.LeavePartyRequest;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.report26.LeaveParty;
import com.agribank.qldvutils.entity.report26.LeavePartyDraft;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LeavePartyService implements EntityHandler {
    private final LeavePartyClient client;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;
    private final CommitteeDecisionService committeeDecisionService;
    private final RequestService requestService;
    private final RequestClient requestClient;
    private final Report26Service report26Service;
    private final UserService userService;
    private final EForm form = EForm.BIEU_26_LEAVE_PARTY;
    private final LeavePartyDraftService leavePartyDraftService;
    private final DvHistoryService dvHistoryService;


    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(LeavePartyDraft.FIELD_MAP);
    }

    public LeavePartyDraft createDraft(LeavePartyRequest request) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        UserDto userDto = userService.findByStaffCodeAndOrganizationCode(request.getStaffCode(), request.getOrganizationCode());
        if (Objects.isNull(userDto)) {
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

        LeaveParty leaveParty = null;
        if (Objects.nonNull(request.getId())){
            leaveParty = client.findById(request.getId()).getData()
                    .orElseThrow(()-> new CommonException("id request không chính xác"));
        }

        LeavePartyDraft requestedDraft = modelMapper.map(request, LeavePartyDraft.class);
        requestedDraft.setStatus(EApprovalStatus.PENDING.getId());
        requestedDraft.setUsernameCreated(userRequested.getUsername());

        requestedDraft.setDeleted(ERecordStatus.ACTIVE.getStatus());

        Request partActivityRequest = requestService.initializeRequest(requestedDraft, leaveParty, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        if (Objects.nonNull(request.getId())){
            requestedDraft.setRefId(request.getId());
        }

        requestedDraft = leavePartyDraftService.save(requestedDraft);

        partActivityRequest.setReferenceId(requestedDraft.getId());
        partActivityRequest.setStaffCode(requestedDraft.getStaffCode());
        partActivityRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return requestedDraft;
    }

    public String createRequestDelete(String id){
        LeaveParty leaveParty = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        Report26 report26 = report26Service.findByRefId(id);
        if (Objects.isNull(report26)){
            throw new CommonException("Sai id, vui lòng kiểm tra lại");
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request partActivityRequest = requestService.initializeRequest(null, leaveParty, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setReferenceId(id);
        partActivityRequest.setStaffCode(report26.getStaffCode());
        partActivityRequest.setOrganizationCode(report26.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return "Tạo yêu cầu thành công";
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        LeavePartyDraft leavePartyDraft = leavePartyDraftService.findById(referenceId);

        leavePartyDraft.setStatus(EApprovalStatus.APPROVED.getId());
        leavePartyDraft.setUsernameAccepted(userDetails.getUsername());

        LeaveParty leaveParty = modelMapper.map(leavePartyDraft, LeaveParty.class);

        leaveParty = client.save(leaveParty).getData();

        Report26 report26 = modelMapper.map(leavePartyDraft, Report26.class);
        report26.setRefId(leaveParty.getId());
        report26.setType(EReport26.LEAVE_PARTY.getId());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        dvHistoryService.saveDV(leavePartyDraft.getStaffCode(),
                EDVStatus.LEAVE_PARTY.getStatus(),
                EDVStatus.LEAVE_PARTY.getName());

        leavePartyDraftService.save(leavePartyDraft);
        report26Service.save(report26);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        UserDetailsImpl userRequested = userService.getUserRequested();

        LeavePartyDraft leavePartyDraft = leavePartyDraftService.findById(referenceId);
        leavePartyDraft.setStatus(EApprovalStatus.APPROVED.getId());
        leavePartyDraft.setUsernameAccepted(userRequested.getUsername());
        LeaveParty leaveParty = modelMapper.map(leavePartyDraft, LeaveParty.class);
        leaveParty.setId(leavePartyDraft.getRefId());

        Report26 report26 = report26Service.findByRefId(leavePartyDraft.getRefId());
        report26.setDecisionNumber(leavePartyDraft.getDecisionNumber());
        report26.setDecisionDate(leavePartyDraft.getDecisionDate());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        leavePartyDraftService.save(leavePartyDraft);
        report26Service.save(report26);
        client.save(leaveParty);
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
        LeavePartyDraft leavePartyDraft = leavePartyDraftService.findById(referenceId);
        leavePartyDraft.setStatus(EApprovalStatus.DENIED.getId());
        leavePartyDraftService.save(leavePartyDraft);
    }

    public RP26DetailResponse getDetail(String id){
        return client.getDetail(id).getData();
    }

    public List<LeaveParty> findAllById(List<String> ids){
        return client.findAllById(ids).getData();
    }
}
