package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.exception.ExceptionMessage;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.gateway.DVDraftClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.request.dv.DVDto;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.log.DVLogService;
import com.agribank.qldvutils.dto.DVCodeNameDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public PageResponse<DVDto> search(SearchDVRequest request){
        PageResponse<DV> dvPageResponse = dvClient.search(request).getData();
        PageResponse<DVDto> response = new PageResponse<>();
        if (Objects.isNull(dvPageResponse)) {
            return response;
        }

        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalItems(dvPageResponse.getTotalItems());

        if (Objects.nonNull(dvPageResponse.getData())) {
            response.setData(dvPageResponse.getData().stream()
                    .map(dv -> modelMapper.map(dv, DVDto.class)
                    ).toList()
            );
        }

        return response;
    }

    public String create(List<DVDto> requests) {
        List<DV> dvs = requests.stream().map(dv -> modelMapper.map(dv, DV.class)).toList();
        DefaultResponse<List<DV>> response = dvClient.saveAll(dvs);

        dvLogService.writeLogRegister(dvs);
        return response.getMessage();
    }

    public DV findById(String id) {
        return dvClient.findById(id).getData();
    }

    public List<DVDto> getDVByOrganization(String organization) {
        if (Objects.isNull(organization)) {
            UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            organization = userRequested.getOrganizationCode();
        }
        List<DV> dvs = dvClient.findByOrganizationCode(organization).getData();
        if (Objects.isNull(dvs) || dvs.isEmpty()) {
            return new ArrayList<>();
        }

        return dvs.stream().map(dv -> modelMapper.map(dv, DVDto.class)).toList();
    }

    public DvDraft create(DVDto dvDto) {
        authorityService.hasAuthorityOverOrganization(dvDto.getOrganizationCode());

        Organization organization = organizationClient.findByCode(dvDto.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DvDraft dvDraft = modelMapper.map(dvDto, DvDraft.class);
        dvDraft.setStatus(EApprovalStatus.PENDING.getId());
        dvDraft.setCreatedBy(userDetails.getId());
        dvDraft = dvDraftClient.save(dvDraft).getData();

        Request request = requestService.initializeRequest(dvDraft, null, form, DV.FIELD_MAP);
        request.setOrganizationCode(dvDto.getOrganizationCode());
        System.out.println("Calling getId(): " + dvDraft.getId());
        request.setStaffCode(dvDraft.getStaffCode());
        request.setReferenceId(dvDraft.getId());
        request.setCreatedBy(userDetails.getId());
        requestClient.save(request);

        return dvDraft;
    }

    public DvDraft update(DVDto dvDto) {
        authorityService.hasAuthorityOverOrganization(dvDto.getOrganizationCode());

        Organization organization = organizationClient.findByCode(dvDto.getOrganizationCode()).getData();
        if (Objects.isNull(organization)) {
            throw new CommonException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DV oldDv = dvClient.findById(dvDto.getId()).getData();

        if (Objects.isNull(oldDv)) {
            throw new CommonException("KOong tim thay thong tin dang vien");
        }

        DvDraft newDV = modelMapper.map(dvDto, DvDraft.class);
        newDV.setStatus(EApprovalStatus.PENDING.getId());
        newDV.setCreatedBy(userDetails.getId());
        newDV = dvDraftClient.save(newDV).getData();

        Request request = requestService.initializeRequest(newDV, oldDv, form, DV.FIELD_MAP);
        request.setOrganizationCode(dvDto.getOrganizationCode());
        request.setStaffCode(oldDv.getStaffCode());
        request.setReferenceId(newDV.getId());
        request.setCreatedBy(userDetails.getId());
        requestClient.save(request);

        return newDV;
    }

    @Override
    public boolean applyCreate(String draftId, UserDetailsImpl userDetails) {
        DvDraft dvDraft = dvDraftClient.findById(draftId).getData()
                .orElse(null);

        if (Objects.isNull(dvDraft) || !Objects.equals(dvDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            return false;
        }

        dvDraft.setStatus(EApprovalStatus.APPROVED.getId());
        dvDraft.setApprovedBy(userDetails.getId());

        DV dv = dvClient.findByStaffCode(dvDraft.getStaffCode()).getData();
        if (Objects.nonNull(dv)) {
            throw new CommonException("Đã tồn tại đảng viên có mã cán bộ: " + dvDraft.getStaffCode());
        }

        dv = new DV();
        mapDVDraftToDV(dv, dvDraft);
        dv.setCreatedBy(dvDraft.getCreatedBy());
        dv.setApprovedBy(userDetails.getId());

        dvClient.save(dv);
        dvDraftClient.save(dvDraft);

        return true;
    }

    @Override
    public boolean applyUpdate(String draftId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        DvDraft dvDraft = dvDraftClient.findById(draftId).getData()
                .orElse(null);

        if (Objects.isNull(dvDraft) || Objects.equals(dvDraft.getStatus(), EApprovalStatus.PENDING.getId())) {
            return false;
        }

        dvDraft.setStatus(EApprovalStatus.APPROVED.getId());
        dvDraft.setApprovedBy(userDetails.getId());

        DV dv = dvClient.findByStaffCode(dvDraft.getStaffCode()).getData();
        mapDVDraftToDV(dv, dvDraft);

        dvClient.save(dv);
        dvDraftClient.save(dvDraft);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        return false;
    }

    @Override
    public void setDenied(String draftId) {
        DvDraft dvDraft = dvDraftClient.findById(draftId).getData()
                .orElse(null);

        dvDraft.setStatus(EApprovalStatus.DENIED.getId());
        dvDraftClient.save(dvDraft);
    }

    public void mapDVDraftToDV(DV dv, DvDraft dvDraft) {
        dv.setStaffCode(dvDraft.getStaffCode());
        dv.setOrganizationCode(dvDraft.getOrganizationCode());
        dv.setResumeNumber(dvDraft.getResumeNumber());
        dv.setPartyCardNumber(dvDraft.getPartyCardNumber());
        dv.setIssueDate(dvDraft.getIssueDate());
        dv.setVneid(dvDraft.getVneid());
        dv.setFullName(dvDraft.getFullName());
        dv.setGender(dvDraft.getGender());
        dv.setUsingName(dvDraft.getUsingName());
        dv.setBirthday(dvDraft.getBirthday());
        dv.setBirthPlace(dvDraft.getBirthPlace());
        dv.setHometown(dvDraft.getHometown());
        dv.setPermanentResidence(dvDraft.getPermanentResidence());
        dv.setTemporaryResidence(dvDraft.getTemporaryResidence());
        dv.setEthnic(dvDraft.getEthnic());
        dv.setReligion(dvDraft.getReligion());
        dv.setFamilyComposition(dvDraft.getFamilyComposition());
        dv.setMartyrsFamily(dvDraft.getMartyrsFamily());
        dv.setRevolution(dvDraft.getRevolution());
        dv.setSocialComposition(dvDraft.getSocialComposition());
        dv.setMainJob(dvDraft.getMainJob());
        dv.setAdmissionDate(dvDraft.getAdmissionDate());
        dv.setSourceRecruitment(dvDraft.getSourceRecruitment());
        dv.setBranchPartyCode(dvDraft.getBranchPartyCode());
        dv.setSuggestionUnion(dvDraft.getSuggestionUnion());
        dv.setSuggestionYouthUnion(dvDraft.getSuggestionYouthUnion());
        dv.setReferrer1(dvDraft.getReferrer1());
        dv.setJobPosition1(dvDraft.getJobPosition1());
        dv.setReferrer2(dvDraft.getReferrer2());
        dv.setJobPosition2(dvDraft.getJobPosition2());
        dv.setOfficialRecognitionDay(dvDraft.getOfficialRecognitionDay());
        dv.setRecruitAnotherOrganization(dvDraft.getRecruitAnotherOrganization());
        dv.setAgriRecruitDate(dvDraft.getAgriRecruitDate());
        dv.setRecruitBrcd(dvDraft.getRecruitBrcd());
        dv.setYouthUnionJoinDate(dvDraft.getYouthUnionJoinDate());
        dv.setOtherSocialOrganization(dvDraft.getOtherSocialOrganization());
        dv.setEnlistmentDate(dvDraft.getEnlistmentDate());
        dv.setDischargeDate(dvDraft.getDischargeDate());
        dv.setDisabledType(dvDraft.getDisabledType());
        dv.setPoliticalIssue(dvDraft.getPoliticalIssue());
        dv.setOldRegime(dvDraft.getOldRegime());
        dv.setFormerWorker(dvDraft.getFormerWorker());
        dv.setForeignMarriage(dvDraft.getForeignMarriage());
        dv.setForeignRelated(dvDraft.getForeignRelated());
        dv.setDegree(dvDraft.getDegree());
        dv.setEducation(dvDraft.getEducation());
        dv.setHealthCondition(dvDraft.getHealthCondition());
        dv.setDateOfDeath(dvDraft.getDateOfDeath());
    }

    public DV findByStaffCode(String staffCode) {
        return dvClient.findByStaffCode(staffCode).getData();
    }

    public DV save(DV dv){
        return dvClient.save(dv).getData();
    }

    public List<DVCodeNameDto> getListProbationaryMemberCodeName(){
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return dvClient.findUnOfficialDV(userDetails.getOrganizationCode()).getData();
    }
}
