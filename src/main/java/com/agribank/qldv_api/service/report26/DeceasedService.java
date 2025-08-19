package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.report26.DeceasedClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.deceased.DeceasedRequest;
import com.agribank.qldv_api.response.report26.DeceasedResponse;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.report26.Deceased;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DeceasedService implements EntityHandler {
    private final DeceasedClient client;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;
    private final RequestService requestService;
    private final RequestClient requestClient;
    private final Report26Service report26Service;
    private final UserService userService;
    private final EForm form = EForm.BIEU_26_DECEASED;
    private final DeceasedDraftService deceasedDraftService;
    private final DvHistoryService dvHistoryService;

    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(PartyActivityExemptionDraft.FIELD_MAP);
    }

    public DeceasedDraft createDraft(DeceasedRequest request) {
        validate(request);
        UserDetailsImpl userRequested = userService.getUserRequested();

        Deceased deceased = null;
        if (Objects.nonNull(request.getId())){
            deceased = client.findById(request.getId()).getData()
                    .orElseThrow(()-> new CommonException("id request không chính xác"));
        }

        DeceasedDraft requestedDraft = modelMapper.map(request, DeceasedDraft.class);
        requestedDraft.setStatus(EApprovalStatus.PENDING.getId());
        requestedDraft.setCreatedBy(userRequested.getId());
        requestedDraft.setDeleted(ERecordStatus.ACTIVE.getStatus());

        Request partActivityRequest = requestService.initializeRequest(requestedDraft, deceased, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        if (Objects.nonNull(request.getId())){
            requestedDraft.setRefId(request.getId());
        }

        requestedDraft = deceasedDraftService.save(requestedDraft);

        partActivityRequest.setReferenceId(requestedDraft.getId());
        partActivityRequest.setStaffCode(requestedDraft.getStaffCode());
        partActivityRequest.setOrganizationCode(request.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return requestedDraft;
    }

    private void validate(DeceasedRequest request){
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
    }

    public String createRequestDelete(String id){
        Deceased deceased = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        Report26 report26 = report26Service.findByRefId(id);
        if (Objects.isNull(report26)){
            throw new CommonException("Sai id, vui lòng kiểm tra lại");
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request partActivityRequest = requestService.initializeRequest(null, deceased, form, getCombinedFieldMap());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setReferenceId(id);
        partActivityRequest.setStaffCode(report26.getStaffCode());
        partActivityRequest.setOrganizationCode(report26.getOrganizationCode());
        requestClient.save(partActivityRequest);

        return "Tạo yêu cầu thành công";
    }
    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {

        DeceasedDraft deceasedDraft = deceasedDraftService.findById(referenceId);

        deceasedDraft.setStatus(EApprovalStatus.APPROVED.getId());
        deceasedDraft.setApprovedBy(userDetails.getId());

        Deceased deceased = modelMapper.map(deceasedDraft, Deceased.class);

        deceased = client.save(deceased).getData();

        Report26 report26 = modelMapper.map(deceasedDraft, Report26.class);
        report26.setRefId(deceased.getId());
        report26.setType(EReport26.DECEASED.getId());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        dvHistoryService.saveDV(deceasedDraft.getStaffCode(),
                EDVStatus.DECEASED.getStatus(),
                EDVStatus.DECEASED.getName());

        deceasedDraftService.save(deceasedDraft);
        report26Service.save(report26);
        return true;
    }


    @Override
    public boolean applyUpdate(String referenceId) {
        UserDetailsImpl userRequested = userService.getUserRequested();

        DeceasedDraft deceasedDraft = deceasedDraftService.findById(referenceId);
        deceasedDraft.setStatus(EApprovalStatus.APPROVED.getId());
        deceasedDraft.setApprovedBy(userRequested.getId());
        Deceased deceased = modelMapper.map(deceasedDraft, Deceased.class);
        deceased.setId(deceasedDraft.getRefId());

        Report26 report26 = report26Service.findByRefId(deceasedDraft.getRefId());
        report26.setDecisionNumber(deceasedDraft.getDecisionNumber());
        report26.setDecisionDate(deceasedDraft.getDecisionDate());
        report26.setDeleted(ERecordStatus.ACTIVE.getStatus());

        deceasedDraftService.save(deceasedDraft);
        report26Service.save(report26);
        client.save(deceased);
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
        DeceasedDraft deceasedDraft = deceasedDraftService.findById(referenceId);
        deceasedDraft.setStatus(EApprovalStatus.DENIED.getId());
        deceasedDraftService.save(deceasedDraft);
    }

    public RP26DetailResponse getDetail(String id){
        return client.getDetail(id).getData();
    }

    public List<Deceased> findAllById(List<String> ids){
        return client.findAllById(ids).getData();
    }

    public DeceasedResponse getDraftDetail(String id){
        DeceasedDraft deceasedDraft = deceasedDraftService.findById(id);

        return modelMapper.map(deceasedDraft, DeceasedResponse.class);
    }

    @SneakyThrows
    public DeceasedResponse updateDraft(DeceasedRequest request) {
        if(Objects.isNull(request.getId()) || request.getId().isBlank()){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        DeceasedDraft  deceasedDraft = deceasedDraftService.findById(request.getId());
        if(Objects.isNull(deceasedDraft)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        Request requestDeceased = requestClient.findByReferenceId(request.getId()).getData();
        if(Objects.isNull(requestDeceased)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        if (EApprovalStatus.PENDING.getId() != requestDeceased.getStatus()){
            throw new CommonException(ExceptionMessage.REQUEST_NOT_PENDING);
        }

        validate(request);
        UserDetailsImpl userRequested = userService.getUserRequested();

        deceasedDraft.setDateOfDeath(request.getDateOfDeath());
        deceasedDraft.setOrganizationCode(request.getOrganizationCode());
        deceasedDraft.setStaffCode(request.getStaffCode());
        deceasedDraft.setDecisionDate(request.getDecisionDate());
        deceasedDraft.setDecisionNumber(request.getDecisionNumber());
        deceasedDraft.setCreatedBy(userRequested.getId());

        requestDeceased.setNewData(requestService.createJsonData(deceasedDraft, getCombinedFieldMap()));
        requestDeceased.setCreatedBy(userRequested.getId());

        deceasedDraftService.save(deceasedDraft);
        requestClient.save(requestDeceased);

        return modelMapper.map(deceasedDraft, DeceasedResponse.class);
    }
}
