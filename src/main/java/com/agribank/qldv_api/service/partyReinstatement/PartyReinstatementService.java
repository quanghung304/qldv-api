package com.agribank.qldv_api.service.partyReinstatement;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.ERecordStatus;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.partyReinstatement.PartyReinstatementClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.partyReinstatement.PartyReinstatementRequest;
import com.agribank.qldv_api.response.partyReinstatement.PartyReinstatementResponse;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.entity.CommitteeDecision;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.partyReinstatement.PartyReinstatement;
import com.agribank.qldvutils.entity.partyReinstatement.PartyReinstatementDraft;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.partyReinstatement.PartyReinstatementSearchRequest;
import com.agribank.qldvutils.request.report26.Report26SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.partyReinstatement.PartyReinstatementDtoResponse;
import com.agribank.qldvutils.response.report26.Report26DtoResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PartyReinstatementService implements EntityHandler {
    private final PartyReinstatementClient client;
    private final RequestClient requestClient;
    private final PartyReinstatementDraftService partyReinstatementDraftService;
    private final UserService userService;
    private final RequestService requestService;
    private final CheckAuthorityService checkAuthorityService;
    private final ModelMapper modelMapper;
    private final CommitteeDecisionService committeeDecisionService;
    private final OrganizationService organizationService;

    private final EForm form = EForm.BIEU_22;

    private Map<String, String> getCombinedFieldMap() {
        return new HashMap<>(PartyReinstatementDraft.FIELD_MAP);
    }

    public String create(PartyReinstatementRequest request) {
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        userService.findByStaffCodeAndOrganizationCode(request.getStaffCode(), request.getOrganizationCode());

        PartyReinstatement partyReinstatement = null;
        if (Objects.nonNull(request.getId())){
            partyReinstatement = findById(request.getId());
        }

        if (Objects.nonNull(request.getId()) && Objects.isNull(partyReinstatement)){
            throw new CommonException("Không tìm thấy bản ghi cần sửa. Vui lòng kiểm tra lại dữ liệu!");
        }

        CommitteeDecision committeeDecision = committeeDecisionService.findByCode(request.getDecisionCommittee());
        if (Objects.isNull(committeeDecision)){
            throw new CommonException("Kiểm tra lại cấp quyết định");
        }

        PartyReinstatementDraft partyReinstatementDraft = modelMapper.map(request, PartyReinstatementDraft.class);
        UserDetailsImpl userRequested = getUserRequested();
        partyReinstatementDraft.setStatus(EApprovalStatus.PENDING.getId());
        partyReinstatementDraft.setUsernameCreated(userRequested.getUsername());

        Request initializedRequest = requestService.initializeRequest(partyReinstatementDraft, partyReinstatement, form, getCombinedFieldMap());
        initializedRequest.setCreatedBy(userRequested.getId());
        if (Objects.nonNull(request.getId())){
            partyReinstatementDraft.setRefId(request.getId());
        }

        partyReinstatementDraft = partyReinstatementDraftService.save(partyReinstatementDraft);

        initializedRequest.setReferenceId(partyReinstatementDraft.getId());
        initializedRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(initializedRequest);

        return "Tạo yêu cầu thành công!";
    }

    public String createDelete(String id) {
        PartyReinstatement partyReinstatement = findById(id);
        if (Objects.isNull(partyReinstatement)){
            throw new CommonException("Không tìm thấy bản ghi cần xóa. Vui lòng kiểm tra lại dữ liệu!");
        }


        UserDetailsImpl userRequested = userService.getUserRequested();
        Request request = requestService.initializeRequest(null, partyReinstatement, form, getCombinedFieldMap());
        request.setCreatedBy(userRequested.getId());
        request.setReferenceId(id);
        request.setOrganizationCode(partyReinstatement.getOrganizationCode());
        requestClient.save(request);

        return "Tạo yêu cầu thành công";
    }

    public PartyReinstatementResponse getDetail(String id) {
        PartyReinstatement partyReinstatement = findById(id);
        if (Objects.isNull(partyReinstatement)){
            throw new CommonException("Không tìm thấy dữ liệu!");
        }

        return modelMapper.map(partyReinstatement, PartyReinstatementResponse.class);
    }

    public PartyReinstatement findById(String id) {
        return client.findById(id).getData().orElse(null);
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public PageResponse<PartyReinstatementDtoResponse> search(PartyReinstatementSearchRequest request){

        request.setOrganizationCode(
                organizationService.validateSearch(request.getOrganizationCode(),
                getUserRequested())
        );

        return client.search(request).getData();
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        PartyReinstatementDraft partyReinstatementDraft = partyReinstatementDraftService.findById(referenceId);
        if (Objects.isNull(partyReinstatementDraft)){
            throw new CommonException("Không tìm thấy bản ghi");
        }

        partyReinstatementDraft.setStatus(EApprovalStatus.APPROVED.getId());
        partyReinstatementDraft.setUsernameAccepted(userDetails.getUsername());

        PartyReinstatement partyReinstatement = modelMapper.map(partyReinstatementDraft, PartyReinstatement.class);

        client.save(partyReinstatement);
        partyReinstatementDraftService.save(partyReinstatementDraft);
        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        PartyReinstatementDraft partyReinstatementDraft = partyReinstatementDraftService.findById(referenceId);
        if (Objects.isNull(partyReinstatementDraft)){
            throw new CommonException("Không tìm thấy bản ghi");
        }

        PartyReinstatement partyReinstatement = findById(partyReinstatementDraft.getRefId());
        if (Objects.isNull(partyReinstatement)){
            throw new CommonException("Không tìm thấy bản ghi");
        }

        UserDetailsImpl userRequested = getUserRequested();
        partyReinstatementDraft.setStatus(EApprovalStatus.APPROVED.getId());
        partyReinstatementDraft.setUsernameAccepted(userRequested.getId());

        partyReinstatement.setOrganizationCode(partyReinstatementDraft.getOrganizationCode());
        partyReinstatement.setStaffCode(partyReinstatementDraft.getStaffCode());
        partyReinstatement.setDecisionCommittee(partyReinstatementDraft.getDecisionCommittee());
        partyReinstatement.setConclusionNumber(partyReinstatementDraft.getConclusionNumber());
        partyReinstatement.setConclusionDate(partyReinstatementDraft.getConclusionDate());
        partyReinstatement.setDecisionNumber(partyReinstatementDraft.getDecisionNumber());
        partyReinstatement.setDecisionDate(partyReinstatementDraft.getDecisionDate());

        client.save(partyReinstatement);
        partyReinstatementDraftService.save(partyReinstatementDraft);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        PartyReinstatement partyReinstatement = findById(referenceId);
        if (Objects.isNull(partyReinstatement)){
            throw new CommonException("Lỗi không tìm thấy bản ghi");
        }

        partyReinstatement.setDeleted(ERecordStatus.DELETED.getStatus());
        client.save(partyReinstatement);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        PartyReinstatementDraft partyReinstatementDraft = partyReinstatementDraftService.findById(referenceId);
        if (Objects.isNull(partyReinstatementDraft)){
            throw new CommonException("Lỗi không tìm thấy bản ghi");
        }

        partyReinstatementDraft.setStatus(EApprovalStatus.DENIED.getId());
        partyReinstatementDraftService.save(partyReinstatementDraft);
    }
}
