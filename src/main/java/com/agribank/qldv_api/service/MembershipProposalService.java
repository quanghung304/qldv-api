package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.ERecordStatus;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.MembershipProposalClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.membership_proposal_draft.MembershipProposalRequest;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.membershipProposal.MPSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp22Response;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import com.agribank.qldvutils.response.membershipProposal.MembershipProposalResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MembershipProposalService implements EntityHandler {
    private final MembershipProposalClient client;
    private final RequestClient requestClient;
    private final CheckAuthorityService checkAuthorityService;
    private final DVService dvService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final MembershipProposalDraftService membershipProposalDraftService;
    private final RequestService requestService;
    private final OrganizationService organizationService;
    private final EmployeeInfoService employeeInfoService;

    private final EForm form = EForm.BIEU_20;

    public Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(MembershipProposalDraft.FIELD_MAP);
    }

    public String create(MembershipProposalRequest request){
        validateMembershipProposal(request);

        MembershipProposal membershipProposal = null;
        if (Objects.nonNull(request.getId())){
            membershipProposal = client.findById(request.getId()).getData().orElse(null);
        }

        if (Objects.nonNull(request.getId()) && Objects.isNull(membershipProposal)){
            throw new CommonException("Không tìm thấy biểu, vui lòng kiểm tra lại id");
        }

        MembershipProposalDraft membershipProposalDraft = modelMapper.map(request, MembershipProposalDraft.class);
        membershipProposalDraft.setOrganizationCode(request.getOrganizationCode());
        membershipProposalDraft.setStatus(EApprovalStatus.PENDING.getId());
        if (Objects.nonNull(membershipProposal)){
            membershipProposalDraft.setRefId(request.getId());
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        membershipProposalDraft.setCreatedBy(userRequested.getUsername());
        membershipProposalDraft = membershipProposalDraftService.save(membershipProposalDraft);

        Request membershipProposalRequest = requestService.initializeRequest(membershipProposalDraft, membershipProposal, form, getCombinedFieldMap());
        membershipProposalRequest.setReferenceId(membershipProposalDraft.getId());
        membershipProposalRequest.setCreatedBy(userRequested.getId());
        membershipProposalRequest.setOrganizationCode(request.getOrganizationCode());
        membershipProposalRequest.setStaffCode(membershipProposalDraft.getStaffCode());
        requestClient.save(membershipProposalRequest);

        return "Tạo yêu cầu thành công";
    }

    private void validateMembershipProposal(MembershipProposalRequest request) {
        EmployeeInfoDto employeeInfoDto = employeeInfoService.findByEmpno(request.getStaffCode());
        if (Objects.isNull(employeeInfoDto)) {
            throw new CommonException("Mã nhân viên không chính xác vui lòng kiểm tra lại!");
        }

        Organization organization = organizationService.findByCode(request.getOrganizationCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Không tìm thấy tổ chức Đảng vui lòng kiểm tra lại");
        }
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
    }

    public String createRequestDelete(String id){
        MembershipProposal membershipProposal = client.findById(id).getData()
                .orElseThrow(()->new CommonException("Sai id, vui lòng kiểm tra lại"));

        UserDetailsImpl userRequested = userService.getUserRequested();
        Request request = requestService.initializeRequest(null, membershipProposal, form, getCombinedFieldMap());
        request.setCreatedBy(userRequested.getId());
        request.setReferenceId(id);
        request.setOrganizationCode(membershipProposal.getOrganizationCode());
        request.setStaffCode(membershipProposal.getStaffCode());
        requestClient.save(request);

        return "Tạo yêu cầu thành công";
    }

    public PageResponse<MembershipProposalResponse> search(MPSearchRequest request){
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        return client.search(request).getData();
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        MembershipProposalDraft membershipProposalDraft = membershipProposalDraftService.findById(referenceId);
        MembershipProposal membershipProposal = modelMapper.map(membershipProposalDraft, MembershipProposal.class);

        membershipProposalDraft.setStatus(EApprovalStatus.APPROVED.getId());
        membershipProposalDraft.setApprovedBy(userDetails.getUsername());

        saveDv(membershipProposal.getStaffCode(),
                membershipProposal.getStaffCode(),
                membershipProposalDraft.getFullName(),
                membershipProposalDraft.getOrganizationCode());
        client.save(membershipProposal);
        membershipProposalDraftService.save(membershipProposalDraft);
        return true;
    }

    private void saveDv(String newStaffCode, String oldStaffCode, String fullName, String organizationCode){
        try {
            EmployeeInfoDto employeeInfoDto = employeeInfoService.findByEmpno(newStaffCode);
            DV dv = dvService.findByStaffCode(oldStaffCode);
            if (Objects.isNull(dv) || Objects.isNull(employeeInfoDto)){
                dv = new DV();
            }

            dv.setStaffCode(newStaffCode);
            dv.setOrganizationCode(organizationCode);
            dv.setFullName(fullName);
            if (Objects.nonNull(employeeInfoDto)){
                dv.setUsingName(employeeInfoDto.getUsingName());
                dv.setVneid(employeeInfoDto.getVneid());
                dv.setBirthday(CommonUtils.timestampConvert(employeeInfoDto.getBirthday()));
                dv.setBirthPlace(employeeInfoDto.getBirthPlace());
                dv.setHometown(employeeInfoDto.getHometown());
                dv.setPermanentResidence(employeeInfoDto.getPermanentResidence());
                dv.setTemporaryResidence(employeeInfoDto.getTemporaryResidence());
            }

            dvService.save(dv);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        MembershipProposalDraft membershipProposalDraft = membershipProposalDraftService.findById(referenceId);
        MembershipProposal membershipProposal = client.findById(membershipProposalDraft.getRefId()).getData()
                .orElseThrow(() -> new CommonException("Không tìm thấy bản ghi"));

        saveDv(membershipProposalDraft.getStaffCode(),
                membershipProposal.getStaffCode(),
                membershipProposalDraft.getFullName(),
                membershipProposalDraft.getOrganizationCode());

        membershipProposal.setOrganizationCode(membershipProposalDraft.getOrganizationCode());
        membershipProposal.setReason(membershipProposalDraft.getReason());
        membershipProposal.setStaffCode(membershipProposalDraft.getStaffCode());
        membershipProposal.setResolutionNumber(membershipProposalDraft.getResolutionNumber());
        membershipProposal.setResolutionDate(membershipProposalDraft.getResolutionDate());
        membershipProposal.setDecisionNumber(membershipProposalDraft.getDecisionNumber());
        membershipProposal.setDecisionDate(membershipProposalDraft.getDecisionDate());

        UserDetailsImpl userRequested = userService.getUserRequested();
        membershipProposalDraft.setStatus(EApprovalStatus.APPROVED.getId());
        membershipProposalDraft.setApprovedBy(userRequested.getUsername());

        client.save(membershipProposal);
        membershipProposalDraftService.save(membershipProposalDraft);
        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        MembershipProposal membershipProposal = client.findById(referenceId).getData()
                .orElseThrow(()->new CommonException("Lỗi"));
        membershipProposal.setDeleted(ERecordStatus.DELETED.getStatus());

        client.save(membershipProposal);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        MembershipProposalDraft membershipProposalDraft = membershipProposalDraftService.findById(referenceId);
        membershipProposalDraft.setStatus(EApprovalStatus.DENIED.getId());

        membershipProposalDraftService.save(membershipProposalDraft);
    }

    public MembershipProposalResponse getDetail(String id){
        MembershipProposal membershipProposal = client.findById(id).getData().orElse(null);

        if (Objects.isNull(membershipProposal)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        DV dv = dvService.findByStaffCode(membershipProposal.getStaffCode());
        MembershipProposalResponse response = modelMapper.map(membershipProposal, MembershipProposalResponse.class);
        if (Objects.nonNull(dv)){
            response.setFullName(dv.getFullName());
        }
        return response;
    }

    public MembershipProposalResponse getDraftDetail(String id) {
        MembershipProposalDraft draft = membershipProposalDraftService.findById(id);
        MembershipProposalResponse response = modelMapper.map(draft, MembershipProposalResponse.class);

        DV dv = dvService.findByStaffCode(draft.getStaffCode());
        if (Objects.nonNull(dv)){
            response.setFullName(dv.getFullName());
        }
        return response;
    }

    public String updateProposalDraft(MembershipProposalRequest request) {
        validateMembershipProposal(request);

        MembershipProposalDraft draft = membershipProposalDraftService.findById(request.getId());

        if (!Objects.equals(draft.getStatus(), EApprovalStatus.PENDING.getId())) {
            throw new CommonException(ExceptionMessage.REQUEST_NOT_PENDING);
        }

        String referenceId = Objects.nonNull(draft.getRefId()) ? draft.getRefId() : null;
        Timestamp createdAt = draft.getCreatedAt();
        UserDetailsImpl userRequested = userService.getUserRequested();

        draft = modelMapper.map(request, MembershipProposalDraft.class);
        draft.setRefId(referenceId);
        draft.setCreatedAt(createdAt);
        draft.setStatus(EApprovalStatus.PENDING.getId());
        draft.setCreatedBy(userRequested.getUsername());

        Request proposalRequest = requestService.getRequestByDraftId(form.getCode(), request.getId());
        String newData = requestService.createJsonData(draft, getCombinedFieldMap());
        proposalRequest.setNewData(newData);

        membershipProposalDraftService.save(draft);
        requestClient.save(proposalRequest);
        return "Cập nhật yêu cầu thành công";
    }

    public PageResponse<DvRp23Response> searchRp23(SearchRpRequest request){
        return client.searchRp23(request).getData();
    }

    public PageResponse<DvRp22Response> searchRp22(SearchRpRequest request){
        return client.searchRp22(request).getData();
    }
}
