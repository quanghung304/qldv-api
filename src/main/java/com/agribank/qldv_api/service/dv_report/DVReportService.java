package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.party_transfer.transfer_temporary.TransferTemporaryClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.dv_report.*;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.response.tcd.Rp17Response;
import com.agribank.qldv_api.service.*;
import com.agribank.qldv_api.service.development_plan.DevelopPlanDetailService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.service.party_reinstatement.PartyReinstatementService;
import com.agribank.qldv_api.service.party_transfer.*;
import com.agribank.qldv_api.service.report26.*;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.Report31Dto;
import com.agribank.qldvutils.dto.SearchRp33Dto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.party_reinstatement.PartyReinstatement;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.entity.report26.*;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10DataRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.report_dv.*;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp22Response;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import com.agribank.qldvutils.response.report07.Report07DtoResponse;
import com.agribank.qldvutils.response.dv_report.DvRp30Response;
import com.agribank.qldvutils.response.Report32Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.agribank.qldv_api.enums.Constants.*;

@Service
@RequiredArgsConstructor
public class DVReportService {
    private final DVService dvService;
    private final RequestService requestService;
    private final DevelopPlanDetailService developPlanDetailService;
    private final OrganizationService organizationService;
    private final DVRecognitionService dvRecognitionService;
    private final Report26Service report26Service;
    private final PartyActivityExemptionService partyActivityExemptionService;
    private final RemoveNamePartyService removeNamePartyService;
    private final LeavePartyService leavePartyService;
    private final DeceasedService deceasedService;
    private final PartyReinstatementService partyReinstatementService;
    private final TransferOutAgribankService transferOutAgribankService;
    private final MembershipProposalService membershipProposalService;
    private final TransferToAgribankService transferToAgribankService;
    private final TransferWithinAgribankService transferWithinAgribankService;
    private final TransferWithinBaseService transferWithinBaseService;
    private final TransferTemporaryClient transferTemporaryClient;
    private final TransferProcessService transferProcessService;
    private final ExportDVRp17Service exportDVRp17Service;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;


    public PageResponse<DvRp24Response> search24(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<DV> dvPageResponse = dvService.searchRp24(request);
        PageResponse<DvRp24Response> response = new PageResponse<>();
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setTotalItems(dvPageResponse.getTotalItems());
        if(Objects.isNull(dvPageResponse.getData()) || dvPageResponse.getData().isEmpty()){
            return response;
        }

        List<DvRp24Response> dvRp24Responses = dvPageResponse.getData().stream()
                .map(dv ->modelMapper.map(dv, DvRp24Response.class)).toList();
        response.setData(dvRp24Responses);

        List<String> staffCodes = dvRp24Responses.stream().map(DvRp24Response::getStaffCode).collect(Collectors.toList());

        List<DVRecognition> dvRecognitions = dvRecognitionService.getDvRByStaffCodeIn(staffCodes);
        if (dvRecognitions.isEmpty()) {
            return response;
        }

        return makeResponse(response, dvRp24Responses, dvRecognitions);
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private PageResponse<DvRp24Response> makeResponse(PageResponse<DvRp24Response> response,
                                                      List<DvRp24Response> dvRp24Responses,
                                                      List<DVRecognition> dvRecognitions){

        Map<String, DVRecognition> dvRecognitionMap = new HashMap<>();
        for (DVRecognition dvRecognition : dvRecognitions) {
            dvRecognitionMap.put(dvRecognition.getStaffCode(), dvRecognition);
        }

        List<Organization> organizations = organizationService.findAll();
        Map<String, Organization> organizationMap = getOrganizationMap(organizations);

        for (DvRp24Response dvRp24Response : dvRp24Responses) {
            DVRecognition dvRecognition = dvRecognitionMap.getOrDefault(dvRp24Response.getStaffCode(), null);
            if(Objects.isNull(dvRecognition)){
                continue;
            }

            dvRp24Response.setConclusionNumber(dvRecognition.getConclusionNumber());
            dvRp24Response.setConclusionDate(dvRecognition.getConclusionDate());
            dvRp24Response.setDecisionNumber(dvRecognition.getDecisionNumber());
            dvRp24Response.setDecisionDate(dvRecognition.getDecisionDate());

            if (Objects.isNull(dvRp24Response.getOrganizationCode())) {
                continue;
            }
            String organizationCodeB = dvRp24Response.getOrganizationCode().substring(0, FORM_B_NAME_LENGTH);

            if (dvRp24Response.getOrganizationCode().length() < FORM_C_NAME_LENGTH){
                continue;
            }
            String organizationCodeC = dvRp24Response.getOrganizationCode().substring(0, FORM_C_NAME_LENGTH);

            dvRp24Response.setOrganizationGroupBName(getOrganizationName(
                    organizationCodeB, EOrganizationReference.GROUP_B.name(), organizationMap));
            dvRp24Response.setOrganizationGroupCName(getOrganizationName(
                    organizationCodeC, EOrganizationReference.GROUP_C.name(), organizationMap));
        }

        response.setData(dvRp24Responses);
        return response;
    }

    private Map<String, Organization> getOrganizationMap(List<Organization> organizations){
        Map<String, Organization> organizationMap = new HashMap<>();
        if (organizations.isEmpty()) {
            return organizationMap;
        }

        for (Organization organization : organizations) {
            organizationMap.put(organization.getCode(), organization);
            if (EOrganizationReference.GROUP_B.getCode().contains(organization.getForm())) {
                organizationMap.put(EOrganizationReference.GROUP_B.name() + "_" +organization.getCode(),
                        organization);
            }else if (EOrganizationReference.GROUP_C.getCode().contains(organization.getForm())) {
                organizationMap.put(EOrganizationReference.GROUP_C.name()+ "_" +organization.getCode(),
                        organization);
            }
        }

        return organizationMap;
    }

    private String getOrganizationName(String organizationCode, String form, Map<String, Organization> organizationMap){
        Organization organization = organizationMap.getOrDefault(
                form + "_" + organizationCode,
                null);
        if (Objects.isNull(organization)) {
            return null;
        }

        return organization.getName();
    }

    public PageResponse<DvRp25Response> search25(SearchRpRequest request){
        PageResponse<DvRp25Response> response = new PageResponse<>();
        PageResponse<PartyReinstatement> partyReinstatementPageResponse = partyReinstatementService.searchRp25(request);
        if (Objects.isNull(partyReinstatementPageResponse.getData()) || partyReinstatementPageResponse.getData().isEmpty()) {
            return response;
        }

        response.setCurrentPage(partyReinstatementPageResponse.getCurrentPage());
        response.setTotalPages(partyReinstatementPageResponse.getTotalPages());
        response.setTotalItems(partyReinstatementPageResponse.getTotalItems());

        List<String> staffCodes = partyReinstatementPageResponse.getData().stream()
                .map(PartyReinstatement::getStaffCode).toList();
        List<DV> dvs = dvService.findByStaffCodeIn(staffCodes);
        if (dvs.isEmpty()) {
            return response;
        }

        return makeResponse25(response, partyReinstatementPageResponse, dvs);
    }

    private PageResponse<DvRp25Response> makeResponse25(PageResponse<DvRp25Response> response, PageResponse<PartyReinstatement> partyReinstatementPageResponse, List<DV> dvs){
        List<DvRp25Response> dvRp25Responses = new ArrayList<>();
        List<Organization> organizations = organizationService.findAll();
        Map<String, Organization> organizationMap = getOrganizationMap(organizations);

        Map<String, DV> dvMap = new HashMap<>();
        for (DV dv : dvs) {
            dvMap.put(dv.getStaffCode(), dv);
        }

        for (PartyReinstatement partyReinstatement : partyReinstatementPageResponse.getData()) {
            DV dv = dvMap.getOrDefault(partyReinstatement.getStaffCode(), null);
            DvRp25Response dvRp25Response = DvRp25Response.builder()
                    .organizationCode(partyReinstatement.getOrganizationCode())
                    .staffCode(partyReinstatement.getStaffCode())
                    .decisionNumber(partyReinstatement.getDecisionNumber())
                    .effectiveDate(partyReinstatement.getEffectiveDate())
                    .build();

            if (Objects.nonNull(dv)){
                dvRp25Response.setFullName(dv.getFullName());
                dvRp25Response.setBirthDay(dv.getBirthday());
                dvRp25Response.setMainJob(dv.getMainJob());
                dvRp25Response.setRecruitBrcd(dv.getRecruitBrcd());
                dvRp25Response.setAdmissionDate(dv.getAdmissionDate());
                dvRp25Response.setOfficialRecognitionDay(dv.getOfficialRecognitionDay());
            }

            if (Objects.isNull(partyReinstatement.getOrganizationCode())){
                dvRp25Responses.add(dvRp25Response);
                continue;
            }
            String organizationCodeB = partyReinstatement.getOrganizationCode().substring(0, FORM_B_NAME_LENGTH);
            dvRp25Response.setOrganizationGroupBName(getOrganizationName(
                    organizationCodeB, EOrganizationReference.GROUP_B.name(), organizationMap));

            if (partyReinstatement.getOrganizationCode().length() < FORM_C_NAME_LENGTH){
                dvRp25Responses.add(dvRp25Response);
                continue;
            }
            String organizationCodeC = partyReinstatement.getOrganizationCode().substring(0, FORM_C_NAME_LENGTH);
            dvRp25Response.setOrganizationGroupCName(getOrganizationName(
                    organizationCodeC, EOrganizationReference.GROUP_C.name(), organizationMap));

            dvRp25Responses.add(dvRp25Response);
        }
        response.setData(dvRp25Responses);

        return response;
    }

    public PageResponse<DvRp34Response> search34(SearchRpRequest request){
        PageResponse<DvRp34Response> response = new PageResponse<>();
        PageResponse<Report26> pageResponse = report26Service.search34(request);

        if (Objects.isNull(pageResponse) || pageResponse.getData().isEmpty()){
            return response;
        }

        response.setCurrentPage(pageResponse.getCurrentPage());
        response.setTotalItems(pageResponse.getTotalItems());
        response.setTotalPages(pageResponse.getTotalPages());

        List<DvRp34Response> dvRp34Responses = new ArrayList<>();

        List<String> partActivityExemptionIds = new ArrayList<>();
        List<String> removeNameIds = new ArrayList<>();
        List<String> leavePartyIds = new ArrayList<>();
        List<String> deceasedIds = new ArrayList<>();
        List<String> staffCodes = new ArrayList<>();

        for (Report26 report26 : pageResponse.getData()) {
            dvRp34Responses.add(DvRp34Response.builder()
                    .id(report26.getRefId())
                    .organizationCode(report26.getOrganizationCode())
                    .staffCode(report26.getStaffCode())
                    .decisionNumber(report26.getDecisionNumber())
                            .effectiveDate(report26.getDecisionDate())
                    .build());

            staffCodes.add(report26.getStaffCode());
            if (EReport26.PARTY_ACTIVITY_EXEMPTION.getId() == report26.getType()){
                partActivityExemptionIds.add(report26.getRefId());
            } else if (EReport26.REMOVE_NAME_PARTY.getId() == report26.getType()) {
                removeNameIds.add(report26.getRefId());
            } else if (EReport26.LEAVE_PARTY.getId() == report26.getType()) {
                leavePartyIds.add(report26.getRefId());
            }else {
                deceasedIds.add(report26.getRefId());
            }
        }

        response.setData(dvRp34Responses);

        List<Organization> organizations = organizationService.findAll();
        Map<String, Organization> organizationMap = getOrganizationMap(organizations);

        Map<String, PartyActivityExemption> partyActivityExemptionMap = getPartyActivityExemptionMap(partActivityExemptionIds);
        Map<String, RemoveNameParty> removeNamePartyMap = getRemoveNamePartyMap(removeNameIds);
        Map<String, LeaveParty> leavePartyMap = getLeavePartyMap(leavePartyIds);
        Map<String, Deceased> deceasedMap = getDeceasedMap(deceasedIds);
        Map<String, DV> dvMap = getDvMap(staffCodes);

        return makeResponseDv34(response, dvRp34Responses, organizationMap, partyActivityExemptionMap, removeNamePartyMap, leavePartyMap, deceasedMap, dvMap);
    }

    private PageResponse<DvRp34Response> makeResponseDv34(PageResponse<DvRp34Response> response,
                                                          List<DvRp34Response> dvRp34Responses,
                                                          Map<String, Organization> organizationMap,
                                                          Map<String, PartyActivityExemption> partyActivityExemptionMap,
                                                          Map<String, RemoveNameParty> removeNamePartyMap,
                                                          Map<String, LeaveParty> leavePartyMap,
                                                          Map<String, Deceased> deceasedMap,
                                                          Map<String, DV> dvMap){
        for (DvRp34Response dvRp34Response : dvRp34Responses) {
            PartyActivityExemption partyActivityExemption = partyActivityExemptionMap.getOrDefault(dvRp34Response.getId(), null);
            if (Objects.nonNull(partyActivityExemption)) {
                dvRp34Response.setTypeName(EReport26.PARTY_ACTIVITY_EXEMPTION.getValue());
                dvRp34Response.setReasonPartyActivityExemption(partyActivityExemption.getReason());
                dvRp34Response.setEffectiveDate(partyActivityExemption.getEffectiveDate());
            }

            RemoveNameParty removeNameParty = removeNamePartyMap.getOrDefault(dvRp34Response.getId(), null);
            if (Objects.nonNull(removeNameParty)) {
                dvRp34Response.setTypeName(EReport26.REMOVE_NAME_PARTY.getValue());
                dvRp34Response.setReasonRemoveNameParty(removeNameParty.getReason());
            }

            LeaveParty leaveParty = leavePartyMap.getOrDefault(dvRp34Response.getId(), null);
            if (Objects.nonNull(leaveParty)) {
                dvRp34Response.setTypeName(EReport26.LEAVE_PARTY.getValue());
                dvRp34Response.setReasonRemoveNameParty(leaveParty.getReason());
            }

            Deceased deceased = deceasedMap.getOrDefault(dvRp34Response.getId(), null);
            if (Objects.nonNull(deceased)) {
                dvRp34Response.setTypeName(EReport26.DECEASED.getValue());
                dvRp34Response.setDateOfDeath(deceased.getDateOfDeath());
            }

            DV dv = dvMap.getOrDefault(dvRp34Response.getStaffCode(), null);
            if (Objects.nonNull(dv)) {
                dvRp34Response.setFullName(dv.getFullName());
                dvRp34Response.setBirthDay(dv.getBirthday());
                dvRp34Response.setMainJob(dv.getMainJob());
                dvRp34Response.setRecruitBrcd(dv.getRecruitBrcd());
            }

            if (Objects.isNull(dvRp34Response.getOrganizationCode())){
                continue;
            }
            String organizationCodeB = dvRp34Response.getOrganizationCode().substring(0, FORM_B_NAME_LENGTH);
            dvRp34Response.setOrganizationGroupBName(getOrganizationName(
                    organizationCodeB, EOrganizationReference.GROUP_B.name(), organizationMap));

            if (dvRp34Response.getOrganizationCode().length() < FORM_C_NAME_LENGTH){
                continue;
            }
            String organizationCodeC = dvRp34Response.getOrganizationCode().substring(0, FORM_C_NAME_LENGTH);
            dvRp34Response.setOrganizationGroupCName(getOrganizationName(
                    organizationCodeC, EOrganizationReference.GROUP_C.name(), organizationMap));
        }

        response.setData(dvRp34Responses);
        return response;
    }

    private Map<String, PartyActivityExemption> getPartyActivityExemptionMap(List<String> partActivityExemptionIds){
        Map<String, PartyActivityExemption> partyActivityExemptionMap = new HashMap<>();
        if (partActivityExemptionIds.isEmpty()) {
            return partyActivityExemptionMap;
        }

        List<PartyActivityExemption> partyActivityExemptions = partyActivityExemptionService.findAllById(partActivityExemptionIds);
        if (partyActivityExemptions.isEmpty()) {
            return partyActivityExemptionMap;
        }

        for (PartyActivityExemption partyActivityExemption : partyActivityExemptions) {
            partyActivityExemptionMap.put(partyActivityExemption.getId(), partyActivityExemption);
        }

        return partyActivityExemptionMap;
    }


    private Map<String, RemoveNameParty> getRemoveNamePartyMap(List<String> removeNamePartyIds){
        Map<String, RemoveNameParty> removeNamePartyHashMap = new HashMap<>();
        if (removeNamePartyIds.isEmpty()) {
            return removeNamePartyHashMap;
        }

        List<RemoveNameParty> removeNameParties = removeNamePartyService.findAllById(removeNamePartyIds);
        if (removeNameParties.isEmpty()) {
            return removeNamePartyHashMap;
        }

        for (RemoveNameParty removeNameParty : removeNameParties) {
            removeNamePartyHashMap.put(removeNameParty.getId(), removeNameParty);
        }

        return removeNamePartyHashMap;
    }

    private Map<String, LeaveParty> getLeavePartyMap(List<String> leavePartyIds){
        Map<String, LeaveParty> leavePartyHashMap = new HashMap<>();
        if (leavePartyIds.isEmpty()) {
            return leavePartyHashMap;
        }

        List<LeaveParty> leaveParties = leavePartyService.findAllById(leavePartyIds);
        if (leaveParties.isEmpty()) {
            return leavePartyHashMap;
        }

        for (LeaveParty leaveParty : leaveParties) {
            leavePartyHashMap.put(leaveParty.getId(), leaveParty);
        }

        return leavePartyHashMap;
    }

    private Map<String, Deceased> getDeceasedMap(List<String> deceasedIds){
        Map<String, Deceased> deceasedHashMap = new HashMap<>();
        if (deceasedIds.isEmpty()) {
            return deceasedHashMap;
        }

        List<Deceased> deceasedList = deceasedService.findAllById(deceasedIds);
        if (deceasedList.isEmpty()) {
            return deceasedHashMap;
        }

        for (Deceased deceased : deceasedList) {
            deceasedHashMap.put(deceased.getId(), deceased);
        }

        return deceasedHashMap;
    }

    private Map<String, DV> getDvMap(List<String> staffCodes){
        Map<String, DV> dvMap = new HashMap<>();
        if (staffCodes.isEmpty()) {
            return dvMap;
        }

        List<DV> dvs = dvService.findByStaffCodeIn(staffCodes);
        if (dvs.isEmpty()) {
            return dvMap;
        }

        for (DV dv : dvs) {
            dvMap.put(dv.getStaffCode(), dv);
        }
        return dvMap;
    }

    public PageResponse<DvRp29Response> search29(SearchReport29Request searchReport29Request){
        PageResponse<TransferOutAgribank> pageResponse;
        SearchRpRequest request = SearchRpRequest.builder()
                .organizationCode(searchReport29Request.getOrganizationCode())
                .fromDate(searchReport29Request.getFromDate())
                .toDate(searchReport29Request.getToDate())
                .build();

        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));
        if (Objects.isNull(searchReport29Request.getType()) || EReport29Type.ALL.getId() == searchReport29Request.getType()) {
            pageResponse = transferOutAgribankService.search29(request);
        } else if (EReport29Type.ON_TIME.getId() == searchReport29Request.getType()) {
            pageResponse = transferOutAgribankService.searchRp29OnTime(request);
        }else {
            pageResponse = transferOutAgribankService.searchRp29Late(request);
        }

        PageResponse<DvRp29Response> response = new PageResponse<>();
        if (Objects.isNull(pageResponse.getData()) || pageResponse.getData().isEmpty()) {
            return response;
        }

        response.setTotalItems(pageResponse.getTotalItems());
        response.setTotalPages(pageResponse.getTotalPages());
        response.setCurrentPage(pageResponse.getCurrentPage());

        List<DvRp29Response> dvRp29Responses = new ArrayList<>();
        List<String> staffCodes = new ArrayList<>();
        for (TransferOutAgribank transfer : pageResponse.getData()) {
            dvRp29Responses.add(DvRp29Response.builder()
                    .staffCode(transfer.getStaffCode())
                    .fullName(transfer.getFullName())
                    .receivedOrganization(transfer.getReceivedOrganization())
                    .transferDate(transfer.getTransferDate())
                    .expectedExpiryDate(transfer.getExpectedExpiryDate())
                    .transferStatus(
                            Objects.isNull(transfer.getExpectedExpiryDate())
                                    || Objects.isNull(transfer.getTransferDate())
                                    ? "Chưa có dữ liệu"
                                    : CommonUtils.validateDatesAfter(
                                    transfer.getTransferDate(), transfer.getExpectedExpiryDate()
                            )
                                    ? "Đúng hạn" : "Quá hạn")
                    .build());


            staffCodes.add(transfer.getStaffCode());
        }
        response.setData(dvRp29Responses);

        return makeResponseRp29(response, staffCodes, dvRp29Responses);
    }

    private PageResponse<DvRp29Response> makeResponseRp29(PageResponse<DvRp29Response> response,
                                                          List<String> staffCodes,
                                                          List<DvRp29Response> dvRp29Responses){
        Map<String, DV> dvMap = getDvMap(staffCodes);
        List<String> organizationCodes = new ArrayList<>();
        for (Map.Entry<String, DV> entry : dvMap.entrySet()) {
            organizationCodes.add(entry.getValue().getOrganizationCode());
        }
        List<Organization> organizations = organizationService.findAllByCode(organizationCodes);
        Map<String, Organization> organizationMap = new HashMap<>();

        for (Organization organization : organizations) {
            organizationMap.put(organization.getCode(), organization);
        }

        for (DvRp29Response dvRp29Response : dvRp29Responses) {
            String code = "";
            DV dv = dvMap.getOrDefault(dvRp29Response.getStaffCode(), null);
            if (Objects.nonNull(dv)){
                code = dv.getOrganizationCode();
                dvRp29Response.setBirthDay(dv.getBirthday());
                dvRp29Response.setMainJob(dv.getMainJob());
                dvRp29Response.setRecruitBrcd(dv.getRecruitBrcd());
                dvRp29Response.setOrganizationCode(code);
            }

            Organization organization = organizationMap.getOrDefault(code, null);
            if (Objects.nonNull(organization)) {
                dvRp29Response.setOrganizationName(organization.getName());
            }
        }

        response.setData(dvRp29Responses);
        return response;
    }

    public PageResponse<DvRp21Response> searchRp21(SearchRpRequest request) {
        PageResponse<DV> dvPageResponse = dvService.searchRp21(request);

        return convertDvPageToRp21Response(dvPageResponse);
    }

    public PageResponse<DvRp21Response> searchRp21b(SearchRpRequest request) {
        PageResponse<DV> dvPageResponse = dvService.searchRp21b(request);

        return convertDvPageToRp21Response(dvPageResponse);
    }

    private PageResponse<DvRp21Response> convertDvPageToRp21Response(PageResponse<DV> dvPageResponse){
        PageResponse<DvRp21Response> response = new PageResponse<>();
        if (Objects.isNull(dvPageResponse) || dvPageResponse.getData().isEmpty()){
            return response;
        }

        List<DvRp21Response> dvRp21Respons = new ArrayList<>();
        List<String> organizationCodes = new ArrayList<>();

        for (DV dv : dvPageResponse.getData()){
            DvRp21Response dvRp21Response = DvRp21Response.builder()
                    .organizationCode(dv.getOrganizationCode())
                    .staffCode(dv.getStaffCode())
                    .fullName(dv.getFullName())
                    .birthDay(dv.getBirthday())
                    .mainJob(dv.getMainJob())
                    .recruitBrcd(dv.getRecruitBrcd())
                    .admissionDate(dv.getAdmissionDate())
                    .recognitionDeadline(CommonUtils.addOneYears(dv.getAdmissionDate()))
                    .build();

            organizationCodes.add(dv.getOrganizationCode());
            dvRp21Respons.add(dvRp21Response);
        }
        response.setData(dvRp21Respons);
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalItems(dvPageResponse.getTotalItems());
        response.setTotalPages(dvPageResponse.getTotalPages());

        return makeResponse21(organizationCodes, dvRp21Respons, response);
    }


    private PageResponse<DvRp21Response> makeResponse21(List<String> organizationCodes, List<DvRp21Response> dvRp21Respons, PageResponse<DvRp21Response> response){
        List<Organization> organizations = organizationService.findAllByCode(organizationCodes);
        Map<String, Organization> organizationMap = getOrganizationMap(organizations);

        for (DvRp21Response dvRp21Response : dvRp21Respons){
            Organization organization = organizationMap.getOrDefault(dvRp21Response.getOrganizationCode(), null);
            if (Objects.isNull(organization)){
                continue;
            }

            if (Objects.isNull(dvRp21Response.getOrganizationCode())){
                continue;
            }
            String organizationCodeB = dvRp21Response.getOrganizationCode().substring(0, FORM_B_NAME_LENGTH);
            dvRp21Response.setOrganizationGroupBName(getOrganizationName(
                    organizationCodeB, EOrganizationReference.GROUP_B.name(), organizationMap));

            if (dvRp21Response.getOrganizationCode().length() < FORM_C_NAME_LENGTH){
                continue;
            }
            String organizationCodeC = dvRp21Response.getOrganizationCode().substring(0, FORM_C_NAME_LENGTH);
            dvRp21Response.setOrganizationGroupCName(getOrganizationName(
                    organizationCodeC, EOrganizationReference.GROUP_C.name(), organizationMap));

        }
        response.setData(dvRp21Respons);
        return response;
    }

    public PageResponse<DvRp23Response> searchRp23(SearchRpRequest request) {
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        return membershipProposalService.searchRp23(request);
    }

    public PageResponse<DvRp28Response> searchRp28(SearchRpRequest request) {
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<DvRp28Response> response = transferToAgribankService.searchRp28(request);

        if (Objects.isNull(response.getData()) || response.getData().isEmpty()) {
            return response;
        }

        List<DvRp28Response> dvRp28Responses = response.getData();
        for (DvRp28Response dvRp28Response : dvRp28Responses){
            dvRp28Response.setTransferStatus( Objects.isNull(dvRp28Response.getExpectedExpiryDate()) || Objects.isNull(dvRp28Response.getTransferDate())
                    ? "Chưa có dữ liệu"
                    : CommonUtils.validateDatesAfter(dvRp28Response.getTransferDate(), dvRp28Response.getExpectedExpiryDate())
                        ? "Đúng hạn" : "Quá hạn");
        }

        response.setData(dvRp28Responses);

        return response;
    }

    public PageResponse<DvRp30Response> searchRp30(SearchRpRequest request) {
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<DvRp30Response> response = transferWithinAgribankService.search30(request);
        if (Objects.isNull(response.getData()) || response.getData().isEmpty()) {
            return response;
        }

        for (DvRp30Response dvRp30Response : response.getData()){
            dvRp30Response.setTransferStatus(Objects.isNull(dvRp30Response.getExpectedExpiryDate()) || Objects.isNull(dvRp30Response.getTransferDate())
                    ? null
                    : CommonUtils.validateDatesAfter(dvRp30Response.getTransferDate(), dvRp30Response.getExpectedExpiryDate())
                        ? "Đúng hạn" : "Quá hạn");
        }

        return response;
    }

    public PageResponse<DvRp22Response> searchRp22(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        return membershipProposalService.searchRp22(request);
    }

    public PageResponse<Report31Dto> search31(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));
        PageResponse<Report31Dto> report31DtoPageResponse = transferWithinBaseService.search31(request);
        return report31DtoPageResponse;
    }


    public PageResponse<SearchRp33Dto> search33(SearchRpRequest request) {
        String organizationCode = Objects.nonNull(request.getOrganizationCode()) ? request.getOrganizationCode() : CommonUtils.getOrganizationByRequestedUser();
        request.setOrganizationCode(organizationCode);
        return transferTemporaryClient.searchRp33(request).getData();
    }

    public PageResponse<Report07DtoResponse> search07(SearchRp07Request request){
        PageResponse<Report07DtoResponse> data = dvService.searchRp07(request);

        SearchRp07Request totalRequest = SearchRp07Request.builder()
                .organizationCode(request.getOrganizationCode())
                .form(request.getForm())
                .type(request.getType())
                .toDate(request.getToDate())
                .build();
        Report07DtoResponse total = dvService.getTotalRp07(totalRequest);

        List<Report07DtoResponse> listResponse = data.getData();
        listResponse.add(total);
        data.setData(listResponse);

        return data;
    }

    public PageResponse<Report32Response> searchRp32(SearchRp32Request request) {
        return transferProcessService.searchRp32(request);
    }

    public PageResponse<BcslDvRp10Response> searchRp10(SearchRpRequest request) {
        PageResponse<BcslDvRp10Response> searchCount = new PageResponse<>();

        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());

        PageResponse<BcslDvRp10Response> dvCount = dvService.searchRp10(request);
        List<String> organizationLists = dvCount.getData().stream().map(BcslDvRp10Response::getOrganizationCode).toList();
        List<BcslDvRp10Response> dvCountList = dvCount.getData();

        Integer form = (Objects.equals(request.getOrganizationCode(), Constants.DANG_UY_AGRIBANK_CODE) ||
                Objects.equals(request.getOrganizationCode(), Constants.BTCDU_CODE)) ? 4 :
                request.getOrganizationCode().length() + 2;

        SearchRp10DataRequest countRequest = SearchRp10DataRequest.builder()
                .organizationCodes(organizationLists)
                .form(form)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();

        List<BcslDvRp10Response> developCount = requestService.searchRp10(countRequest);

        List<BcslDvRp10Response> planCount = developPlanDetailService.searchRp10(countRequest);

        List<BcslDvRp10Response> transferCount = transferProcessService.searchRp10(countRequest);

        dvCountList.addAll(developCount);
        dvCountList.addAll(planCount);
        dvCountList.addAll(transferCount);

        Map<String, BcslDvRp10Response> mergedMap = new HashMap<>();

        for (BcslDvRp10Response count : dvCountList) {
            if (!mergedMap.containsKey(count.getOrganizationCode())) {
                mergedMap.put(count.getOrganizationCode(), count);
            } else {
                mergedMap.get(count.getOrganizationCode()).merge(count);
            }
        }
        List<BcslDvRp10Response> listFormat = new ArrayList<>(formatData(new ArrayList<>(mergedMap.values())));
        listFormat.sort(Comparator.comparing(obj -> Integer.parseInt(obj.getOrganizationCode())));
        listFormat.add(getTotalCount(listFormat));

        searchCount.setData(listFormat);
        searchCount.setTotalItems(dvCount.getTotalItems());
        searchCount.setTotalPages(dvCount.getTotalPages());
        searchCount.setCurrentPage(dvCount.getCurrentPage());

        return searchCount;
    }

    private List<BcslDvRp10Response> formatData(List<BcslDvRp10Response> listSearch) {
        return listSearch.stream().map(data -> new BcslDvRp10Response(
                data.getOrganizationCode(),
                data.getOrganizationName(),
                data.getTotalBefore() != null ? data.getTotalBefore() : 0,
                data.getDevelopPlan() != null ? data.getDevelopPlan() : 0,
                (data.getAdmissionCount() != null ? data.getAdmissionCount() : 0) + (data.getTransferToAgribank() != null ? data.getTransferToAgribank() : 0),
                data.getAdmissionCount() != null ? data.getAdmissionCount() : 0,
                data.getTransferToAgribank() != null ? data.getTransferToAgribank() : 0,
                data.getMembershipRestore() != null ? data.getMembershipRestore() : 0,
                (data.getLeaveCount() != null ? data.getLeaveCount() : 0) + (data.getRemoveCount() != null ? data.getRemoveCount() : 0) +
                        (data.getDisciplineCount() != null ? data.getDisciplineCount() : 0) + (data.getTransferOutAgribank() != null ? data.getTransferOutAgribank() : 0) +
                        (data.getDecreasedCount() != null ? data.getDecreasedCount() : 0),
                data.getLeaveCount() != null ? data.getLeaveCount() : 0,
                data.getRemoveCount() != null ? data.getRemoveCount() : 0,
                data.getDisciplineCount() != null ? data.getDisciplineCount() : 0,
                data.getTransferOutAgribank() != null ? data.getTransferOutAgribank() : 0,
                data.getDecreasedCount() != null ? data.getDecreasedCount() : 0,
                data.getRecognizeCount() != null ? data.getRecognizeCount() : 0,
                data.getWaitRecognize() != null ? data.getWaitRecognize() : 0,
                data.getTransferWithinAgribank() != null ? data.getTransferWithinAgribank() : 0,
                data.getTransferTemporary() != null ? data.getTransferTemporary() : 0,
                data.getTransferProcessing() != null ? data.getTransferProcessing() : 0,
                data.getExemptionCount() != null ? data.getExemptionCount() : 0,
                data.getTotalAfter() != null ? data.getTotalAfter() : 0,
                (data.getDevelopPlan() == null || data.getDevelopPlan() == 0 || data.getAdmissionCount() == null || data.getAdmissionCount() == 0) ?
                        0 : (data.getAdmissionCount().doubleValue() / data.getDevelopPlan().doubleValue()) * 100
        )).toList();
    }

    private BcslDvRp10Response getTotalCount(List<BcslDvRp10Response> list) {
        BcslDvRp10Response bcslRp10Response = BcslDvRp10Response.builder()
                .organizationName("Tổng cộng: ")
                .totalBefore(0)
                .developPlan(0)
                .totalIncrease(0)
                .admissionCount(0)
                .transferToAgribank(0)
                .membershipRestore(0)
                .totalDecrease(0)
                .leaveCount(0)
                .removeCount(0)
                .disciplineCount(0)
                .transferOutAgribank(0)
                .decreasedCount(0)
                .recognizeCount(0)
                .waitRecognize(0)
                .transferWithinAgribank(0)
                .transferTemporary(0)
                .transferProcessing(0)
                .exemptionCount(0)
                .totalAfter(0)
                .build();
        for (BcslDvRp10Response data : list){
            Integer totalBefore = bcslRp10Response.getTotalBefore() + data.getTotalBefore();
            bcslRp10Response.setTotalBefore(totalBefore);

            Integer developPlan = bcslRp10Response.getDevelopPlan() + data.getDevelopPlan();
            bcslRp10Response.setDevelopPlan(developPlan);

            Integer totalIncrease = bcslRp10Response.getTotalIncrease() + data.getTotalIncrease();
            bcslRp10Response.setTotalIncrease(totalIncrease);

            Integer admissionCount = bcslRp10Response.getAdmissionCount() + data.getAdmissionCount();
            bcslRp10Response.setAdmissionCount(admissionCount);

            Integer transferToAgribank = bcslRp10Response.getTransferToAgribank() + data.getTransferToAgribank();
            bcslRp10Response.setTransferToAgribank(transferToAgribank);

            Integer membershipRestore = bcslRp10Response.getMembershipRestore() + data.getMembershipRestore();
            bcslRp10Response.setMembershipRestore(membershipRestore);

            Integer totalDecrease = bcslRp10Response.getTotalDecrease() + data.getTotalDecrease();
            bcslRp10Response.setTotalDecrease(totalDecrease);

            Integer leaveCount = bcslRp10Response.getLeaveCount() + data.getLeaveCount();
            bcslRp10Response.setLeaveCount(leaveCount);

            Integer removeCount = bcslRp10Response.getRemoveCount() + data.getRemoveCount();
            bcslRp10Response.setRemoveCount(removeCount);

            Integer disciplineCount = bcslRp10Response.getDisciplineCount() + data.getDisciplineCount();
            bcslRp10Response.setDisciplineCount(disciplineCount);

            Integer transferOutAgribank = bcslRp10Response.getTransferOutAgribank() + data.getTransferOutAgribank();
            bcslRp10Response.setTransferOutAgribank(transferOutAgribank);

            Integer decreasedCount = bcslRp10Response.getDecreasedCount() + data.getDecreasedCount();
            bcslRp10Response.setDecreasedCount(decreasedCount);

            Integer recognizeCount = bcslRp10Response.getRecognizeCount() + data.getRecognizeCount();
            bcslRp10Response.setRecognizeCount(recognizeCount);

            Integer waitRecognize = bcslRp10Response.getWaitRecognize() + data.getWaitRecognize();
            bcslRp10Response.setWaitRecognize(waitRecognize);

            Integer transferWithinAgribank = bcslRp10Response.getTransferWithinAgribank() + data.getTransferWithinAgribank();
            bcslRp10Response.setTransferWithinAgribank(transferWithinAgribank);

            Integer transferTemporary = bcslRp10Response.getTransferTemporary() + data.getTransferTemporary();
            bcslRp10Response.setTransferTemporary(transferTemporary);

            Integer transferProcessing = bcslRp10Response.getTransferProcessing() + data.getTransferProcessing();
            bcslRp10Response.setTransferProcessing(transferProcessing);

            Integer exemptionCount = bcslRp10Response.getExemptionCount() + data.getExemptionCount();
            bcslRp10Response.setExemptionCount(exemptionCount);

            Integer totalAfter = bcslRp10Response.getTotalAfter() + data.getTotalAfter();
            bcslRp10Response.setTotalAfter(totalAfter);
        }

        Double admissionPercent = (bcslRp10Response.getDevelopPlan() == 0 || bcslRp10Response.getAdmissionCount() == 0) ?
                0 : (bcslRp10Response.getAdmissionCount().doubleValue() / bcslRp10Response.getDevelopPlan().doubleValue()) * 100;
        bcslRp10Response.setAdmissionPercent(admissionPercent);
        return bcslRp10Response;
    }

    public PageResponse<Rp17Response> searchRp17(SearchRp17Request request) {
        if (Objects.isNull(request.getFromDate())) {
            throw new CommonException("Vui lòng chọn từ thời điểm báo cáo");
        }

        if (Objects.isNull(request.getToDate())) {
            throw new CommonException("Vui lòng chọn đến thời điểm báo cáo");
        }
        String organizationCode = Objects.nonNull(request.getOrganizationCode()) ? request.getOrganizationCode() : CommonUtils.getOrganizationByRequestedUser();
        request.setOrganizationCode(organizationCode);
        return dvService.searchRp17(request);
    }

    public ExportResponse exportExcelRp17(SearchRp17Request request) {
        try {
            return exportDVRp17Service.exportData(request, EExcelColumnInfo.BC_17_DSDV.getName(), "", EExcelColumnInfo.BC_17_DSDV.name(), 1, EExcelColumnInfo.BC_17_DSDV.getName());
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }

    public PDFContentResult exportPDFRp17(SearchRp17Request request) {
        try {
            return exportDVRp17Service.exportPDFData(request, EExcelColumnInfo.BC_17_DSDV.getName(), "", EExcelColumnInfo.BC_17_DSDV.getName(), 1, EExcelColumnInfo.BC_17_DSDV.getPageType());
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
