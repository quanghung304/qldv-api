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
import com.agribank.qldv_api.response.dv.DVResponse;
import com.agribank.qldv_api.response.tcd.Rp17Response;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.log.DVLogService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.DVCodeNameDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp07Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.DVSearchResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.DvRp18Response;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import com.agribank.qldvutils.response.report07.Report07DtoResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    private final OrganizationService organizationService;
    private final DvOrgService dvOrgService;

    private static final EForm form = EForm.BIEU_15;

    public PageResponse<DVSearchResponse> search(SearchDVRequest request){
        if (Objects.isNull(request.getOrganizationCode())) {
            request.setOrganizationCode(CommonUtils.getOrganizationByRequestedUser());
        } else {
            authorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        }

        return dvClient.search(request).getData();
    }

    public String create(List<DVDto> requests) {
        List<DV> dvs = requests.stream().map(dv -> modelMapper.map(dv, DV.class)).toList();
        DefaultResponse<List<DV>> response = dvClient.saveAll(dvs);

        dvLogService.writeLogRegister(dvs);
        return response.getMessage();
    }

    public DVDto findById(String id) {
        DV dv = dvClient.findById(id).getData();
        Organization organization = organizationClient.findByCode(dv.getOrganizationCode()).getData();

        DVDto dvDto = modelMapper.map(dv, DVDto.class);
        dvDto.setOrganizationName(organization.getName());
        return dvDto;
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
            throw new CommonException("Không tìm thấy thông tin đảng viên");
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
        if (Objects.nonNull(dvDraft.getOrganizationCode()) && !Objects.equals(dvDraft.getOrganizationCode(), dv.getOrganizationCode())) {
            DvOrgHistory dvOrgHistory = DvOrgHistory.builder()
                    .oldOrgCode(dv.getOrganizationCode())
                    .newOrgCode(dvDraft.getOrganizationCode())
                    .build();

            dvOrgService.save(dvOrgHistory);
        }
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

    public List<DVResponse> getDVByPartyReinstatement(){
        List<DV> dvs = dvClient.getDVByPartyReinstatement().getData();
        if(dvs.isEmpty()){
            return new ArrayList<>();
        }

        return dvs.stream().map(dv -> modelMapper.map(dv, DVResponse.class)).collect(Collectors.toList());
    }

    public PageResponse<Rp17Response> searchRp17(SearchRp17Request request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));
        PageResponse<DV> dvPageResponse = getSearch17(request);
        PageResponse<Rp17Response> response = new PageResponse<>();
        if (Objects.isNull(dvPageResponse)) {
            return response;
        }

        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalItems(dvPageResponse.getTotalItems());

        if (Objects.nonNull(dvPageResponse.getData())) {
            response.setData(dvPageResponse.getData().stream()
                    .map(dv -> modelMapper.map(dv, Rp17Response.class)
                    ).toList()
            );
        }

        return response;
    }

    public PageResponse<DV> getSearch17(SearchRp17Request request){
        return dvClient.searchRp17(request).getData();
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<DvDraft> createManyDV(List<DvDraft> dvDrafts){
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        dvDrafts = dvDraftClient.saveAll(dvDrafts).getData();
        List<Request> saveRequests = new ArrayList<>();
        for (DvDraft dvDraft: dvDrafts){
            Request request = requestService.initializeRequest(dvDraft, null, form, DV.FIELD_MAP);
            request.setOrganizationCode(dvDraft.getOrganizationCode());
            request.setStaffCode(dvDraft.getStaffCode());
            request.setReferenceId(dvDraft.getId());
            request.setCreatedBy(userDetails.getId());
            saveRequests.add(request);
        }

        requestClient.saveAll(saveRequests);

        return dvDrafts;
    }

    public List<DvDraft> updateManyDV(List<DV> oldDVs, List<DvDraft> dvDrafts){
        List<Request> saveRequests = new ArrayList<>();
        List<DvDraft> newDVDrafts = new ArrayList<>();

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        for (int i = 0; i < oldDVs.size(); i++) {
            DvDraft draft = dvDrafts.get(i);
            DV oldDv = oldDVs.get(i);
            DvDraft newDV = new DvDraft();
            modelMapper.map(oldDv, newDV);

            //
            newDV.setOrganizationCode(draft.getOrganizationCode());
            newDV.setStaffCode(draft.getStaffCode());
            newDV.setResumeNumber(draft.getResumeNumber());
            newDV.setPartyCardNumber(draft.getPartyCardNumber());
            newDV.setFullName(draft.getFullName());
            newDV.setUsingName(draft.getUsingName());
            newDV.setBirthday(draft.getBirthday());
            newDV.setAdmissionDate(draft.getAdmissionDate());
            newDV.setOfficialRecognitionDay(draft.getOfficialRecognitionDay());
            newDV.setGender(draft.getGender());
            newDV.setEthnic(draft.getEthnic());
            newDV.setReligion(draft.getReligion());

            newDV.setStatus(EApprovalStatus.PENDING.getId());
            newDV.setCreatedBy(userDetails.getId());

            newDVDrafts.add(newDV);
        }

        newDVDrafts = dvDraftClient.saveAll(newDVDrafts).getData();

        for (int i = 0; i < oldDVs.size(); i++) {
            DV oldDv = oldDVs.get(i);
            DvDraft newDV = newDVDrafts.get(i);

            Request request = requestService.initializeRequest(newDV, oldDv, form, DV.FIELD_MAP);
            request.setOrganizationCode(newDV.getOrganizationCode());
            request.setStaffCode(oldDv.getStaffCode());
            request.setReferenceId(newDV.getId());
            request.setCreatedBy(userDetails.getId());

            saveRequests.add(request);
        }

        requestClient.saveAll(saveRequests);

        return dvDrafts;
    }


    public PageResponse<DV> searchRp24(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));
        return dvClient.searchRp24(request).getData();
    }

    public List<DV> findByStaffCodeIn(List<String> staffCodes){
        return dvClient.findByStaffCodes(staffCodes).getData();
    }

    public PageResponse<DV> searchRp21(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));
        return dvClient.searchRp21(request).getData();
    }

    public List<DV> saveAll(List<DV> dvs){
        return dvClient.saveAll(dvs).getData();
    }

    public List<DV> findByOrganizationCodeActiveIn(List<String> organizationCodes){
        return dvClient.findByOrganizationCodeActiveIn(organizationCodes).getData();
    }

    public List<DVDto> findActiveDVByOrganizationCode(String organization) {
        if (Objects.isNull(organization)) {
            UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            organization = userRequested.getOrganizationCode();
        }
        List<DV> dvs = getActiveDVByOrganizationCode(organization);
        if (Objects.isNull(dvs) || dvs.isEmpty()) {
            return new ArrayList<>();
        }

        return dvs.stream().map(dv -> modelMapper.map(dv, DVDto.class)).toList();
    }

    public List<DV> getActiveDVByOrganizationCode(String organizationCode){
        return dvClient.findActiveDVByOrganizationCode(organizationCode).getData();
    }

    public PageResponse<Report07DtoResponse> searchRp07(SearchRp07Request request){
        authorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        return dvClient.searchRp07(request).getData();
    }

    public Report07DtoResponse getTotalRp07(SearchRp07Request request){
        return dvClient.getTotalRp07(request).getData();
    }
    public PageResponse<DvRp18Response> searchRp18(SearchRequest request){
        return dvClient.searchRp18(request).getData();
    }

    public PageResponse<BcslDvRp10Response> searchRp10(SearchRpRequest request){
        return organizationClient.searchRp10(request).getData();
    }

    public DVResponse getDraftDetail(String id){
        DvDraft dvDraft = dvDraftClient.findById(id)
                .getData().orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        return modelMapper.map(dvDraft, DVResponse.class);
    }

    @SneakyThrows
    public DVResponse updateDraft(DVDto request){
        if (Objects.isNull(request.getId()) || request.getId().isBlank()) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        DvDraft dvDraft = dvDraftClient.findById(request.getId())
                .getData().orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        Request requestDv = requestClient.findByReferenceId(request.getId()).getData();
        if (Objects.isNull(requestDv)) {
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        if (EApprovalStatus.PENDING.getId() != requestDv.getStatus()) {
            throw new CommonException(ExceptionMessage.REQUEST_NOT_PENDING);
        }

        UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mapDVDraft(dvDraft, request);
        dvDraft.setCreatedBy(userRequested.getId());

        requestDv.setNewData(requestService.createJsonData(dvDraft, DV.FIELD_MAP));
        requestDv.setCreatedBy(userRequested.getId());

        dvDraftClient.save(dvDraft);
        requestClient.save(requestDv);

        return modelMapper.map(dvDraft, DVResponse.class);
    }

    public void mapDVDraft(DvDraft dvDraft, DVDto request) {
        dvDraft.setStaffCode(request.getStaffCode());
        dvDraft.setOrganizationCode(request.getOrganizationCode());
        dvDraft.setResumeNumber(request.getResumeNumber());
        dvDraft.setPartyCardNumber(request.getPartyCardNumber());
        dvDraft.setIssueDate(request.getIssueDate());
        dvDraft.setVneid(request.getVneid());
        dvDraft.setFullName(request.getFullName());
        dvDraft.setGender(request.getGender());
        dvDraft.setUsingName(request.getUsingName());
        dvDraft.setBirthday(request.getBirthday());
        dvDraft.setBirthPlace(request.getBirthPlace());
        dvDraft.setHometown(request.getHometown());
        dvDraft.setPermanentResidence(request.getPermanentResidence());
        dvDraft.setTemporaryResidence(request.getTemporaryResidence());
        dvDraft.setEthnic(request.getEthnic());
        dvDraft.setReligion(request.getReligion());
        dvDraft.setFamilyComposition(request.getFamilyComposition());
        dvDraft.setMartyrsFamily(request.getMartyrsFamily());
        dvDraft.setRevolution(request.getRevolution());
        dvDraft.setSocialComposition(request.getSocialComposition());
        dvDraft.setMainJob(request.getMainJob());
        dvDraft.setAdmissionDate(request.getAdmissionDate());
        dvDraft.setSourceRecruitment(request.getSourceRecruitment());
        dvDraft.setBranchPartyCode(request.getBranchPartyCode());
        dvDraft.setSuggestionUnion(request.getSuggestionUnion());
        dvDraft.setSuggestionYouthUnion(request.getSuggestionYouthUnion());
        dvDraft.setReferrer1(request.getReferrer1());
        dvDraft.setJobPosition1(request.getJobPosition1());
        dvDraft.setReferrer2(request.getReferrer2());
        dvDraft.setJobPosition2(request.getJobPosition2());
        dvDraft.setOfficialRecognitionDay(request.getOfficialRecognitionDay());
        dvDraft.setRecruitAnotherOrganization(request.getRecruitAnotherOrganization());
        dvDraft.setAgriRecruitDate(request.getAgriRecruitDate());
        dvDraft.setRecruitBrcd(request.getRecruitBrcd());
        dvDraft.setYouthUnionJoinDate(request.getYouthUnionJoinDate());
        dvDraft.setOtherSocialOrganization(request.getOtherSocialOrganization());
        dvDraft.setEnlistmentDate(request.getEnlistmentDate());
        dvDraft.setDischargeDate(request.getDischargeDate());
        dvDraft.setDisabledType(request.getDisabledType());
        dvDraft.setPoliticalIssue(request.getPoliticalIssue());
        dvDraft.setOldRegime(request.getOldRegime());
        dvDraft.setFormerWorker(request.getFormerWorker());
        dvDraft.setForeignMarriage(request.getForeignMarriage());
        dvDraft.setForeignRelated(request.getForeignRelated());
        dvDraft.setDegree(request.getDegree());
        dvDraft.setEducation(request.getEducation());
        dvDraft.setHealthCondition(request.getHealthCondition());
        dvDraft.setDateOfDeath(request.getDateOfDeath());
    }
}
