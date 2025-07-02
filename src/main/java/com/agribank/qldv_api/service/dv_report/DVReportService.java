package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.enums.EOrganizationReference;
import com.agribank.qldv_api.enums.EReport26;
import com.agribank.qldv_api.enums.EReport29Type;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.dv_report.DvRp24Response;
import com.agribank.qldv_api.response.dv_report.DvRp29Response;
import com.agribank.qldv_api.response.dv_report.DvRp34Response;
import com.agribank.qldv_api.response.dv_report.DvRp25Response;
import com.agribank.qldv_api.service.DVRecognitionService;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.service.party_reinstatement.PartyReinstatementService;
import com.agribank.qldv_api.service.party_transfer.TransferOutAgribankService;
import com.agribank.qldv_api.service.report26.*;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.party_reinstatement.PartyReinstatement;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.entity.report26.*;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.request.report_dv.SearchRp29Request;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.response.PageResponse;
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
    private final OrganizationService organizationService;
    private final DVRecognitionService dvRecognitionService;
    private final Report26Service report26Service;
    private final PartyActivityExemptionService partyActivityExemptionService;
    private final RemoveNamePartyService removeNamePartyService;
    private final LeavePartyService leavePartyService;
    private final DeceasedService deceasedService;
    private final PartyReinstatementService partyReinstatementService;
    private final TransferOutAgribankService transferOutAgribankService;
    private final ModelMapper modelMapper;


    public PageResponse<DvRp24Response> search24(SearchRp24Request request){
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

    public PageResponse<DvRp25Response> search25(SearchRp25Request request){
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

    public PageResponse<DvRp34Response> search34(SearchRp34Request request){
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
        PageResponse<TransferOutAgribank> pageResponse = new PageResponse<>();
        SearchRp29Request request = SearchRp29Request.builder()
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
                                    ? "Chưa có dữ liệu ngày dự kiến và ngày Chuyển ra ngoài Đảng bộ Agribank"
                                    : CommonUtils.validateDatesAfter(
                                            transfer.getExpectedExpiryDate(), transfer.getTransferDate()
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
}
