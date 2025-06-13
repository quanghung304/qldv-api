package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.report26.PartyActivityExemptionClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.party_activity_exemption.PartyActivityExemptionRequest;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.report26.PartyActivityExemption;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class PartyActivityExemptionService implements EntityHandler {
    private final PartyActivityExemptionClient client;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;
    private final CommitteeDecisionService committeeDecisionService;
    private final RequestService requestService;
    private final EForm form = EForm.BIEU_26_PARTY_ACTIVITY_EXEMPTION;
    private final RequestClient requestClient;
    private final Report26Service report26Service;
    private final UserService userService;
    private final PartyActivityExemptionDraftService partyActivityExemptionDraftService;
    private final DvHistoryService dvHistoryService;

    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(PartyActivityExemptionDraft.FIELD_MAP);
    }

    public PartyActivityExemptionDraft createDraft(PartyActivityExemptionRequest request){
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
        UserDetailsImpl userRequested = getUserRequested();


        CommitteeDecision committeeDecision = committeeDecisionService.findByCode(request.getCommitteeDecision());
        if (Objects.isNull(committeeDecision)){
            throw new CommonException("Sai code cấp ủy quyết định. Vui lòng kiểm tra lại");
        }

        PartyActivityExemption partyActivityExemption = null;
        if (Objects.nonNull(request.getId())){
            partyActivityExemption = client.findById(request.getId()).getData()
                    .orElseThrow(()-> new CommonException("id request không chính xác"));
        }

        PartyActivityExemptionDraft requestedDraft = modelMapper.map(request, PartyActivityExemptionDraft.class);
        requestedDraft.setStatus(EApprovalStatus.PENDING.getId());
        requestedDraft.setUsernameCreated(userRequested.getUsername());
        requestedDraft.setDeleted(ERecordStatus.ACTIVE.getStatus());

        Request partActivityRequest = requestService.initializeRequest(requestedDraft, partyActivityExemption, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setOrganizationCode(request.getOrganizationCode());
        partActivityRequest.setStaffCode(requestedDraft.getStaffCode());
        if (Objects.nonNull(request.getId())){
            requestedDraft.setRefId(request.getId());
        }

        requestedDraft = partyActivityExemptionDraftService.save(requestedDraft);

        partActivityRequest.setReferenceId(requestedDraft.getId());
        requestClient.save(partActivityRequest);

        return requestedDraft;
    }

    public String createRequestDelete(String id){
        PartyActivityExemption partyActivityExemption = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        Report26 report26 = report26Service.findByRefId(id);
        if (Objects.isNull(report26)){
            throw new CommonException("Sai id, vui lòng kiểm tra lại");
        }
        checkAuthorityService.hasAuthorityOverOrganization(report26.getOrganizationCode());

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request partActivityRequest = requestService.initializeRequest(null, partyActivityExemption, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setReferenceId(id);
        partActivityRequest.setOrganizationCode(report26.getOrganizationCode());
        partActivityRequest.setStaffCode(report26.getStaffCode());
        requestClient.save(partActivityRequest);

        return "Tạo yêu cầu thành công";
    }


    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        PartyActivityExemptionDraft partyActivityExemptionDraft = partyActivityExemptionDraftService.findById(referenceId);

        partyActivityExemptionDraft.setStatus(EApprovalStatus.APPROVED.getId());
        partyActivityExemptionDraft.setUsernameAccepted(userDetails.getUsername());

        PartyActivityExemption partyActivityExemption = PartyActivityExemption.builder()
                .committeeDecision(partyActivityExemptionDraft.getCommitteeDecision())
                .effectiveDate(partyActivityExemptionDraft.getEffectiveDate())
                .reason(partyActivityExemptionDraft.getReason())
                .build();

        partyActivityExemption = client.save(partyActivityExemption).getData();

        Report26 report26 = modelMapper.map(partyActivityExemptionDraft, Report26.class);
        report26.setRefId(partyActivityExemption.getId());
        report26.setType(EReport26.PARTY_ACTIVITY_EXEMPTION.getId());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        dvHistoryService.saveDV(partyActivityExemptionDraft.getStaffCode(),
                EDVStatus.PARTY_ACTIVITY_EXEMPTION.getStatus(),
                EDVStatus.PARTY_ACTIVITY_EXEMPTION.getName());
        partyActivityExemptionDraftService.save(partyActivityExemptionDraft);
        report26Service.save(report26);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        UserDetailsImpl userRequested = getUserRequested();

        PartyActivityExemptionDraft partyActivityExemptionDraft = partyActivityExemptionDraftService.findById(referenceId);
        partyActivityExemptionDraft.setStatus(EApprovalStatus.APPROVED.getId());
        partyActivityExemptionDraft.setUsernameAccepted(userRequested.getUsername());
        PartyActivityExemption partyActivityExemption = modelMapper.map(partyActivityExemptionDraft, PartyActivityExemption.class);
        partyActivityExemption.setId(partyActivityExemptionDraft.getRefId());

        Report26 report26 = report26Service.findByRefId(partyActivityExemptionDraft.getRefId());
        report26.setDecisionNumber(partyActivityExemptionDraft.getDecisionNumber());
        report26.setDecisionDate(partyActivityExemptionDraft.getDecisionDate());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        partyActivityExemptionDraftService.save(partyActivityExemptionDraft);
        report26Service.save(report26);
        client.save(partyActivityExemption);
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
        PartyActivityExemptionDraft partyActivityExemptionDraft = partyActivityExemptionDraftService.findById(referenceId);
        partyActivityExemptionDraft.setStatus(EApprovalStatus.DENIED.getId());
        partyActivityExemptionDraftService.save(partyActivityExemptionDraft);
    }

    private UserDetailsImpl getUserRequested(){
        return  (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public RP26DetailResponse getDetail(String id){
        return client.getDetail(id).getData();
    }

}
