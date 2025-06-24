package com.agribank.qldv_api.service.dv_report;

import com.agribank.qldv_api.enums.EOrganizationReference;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.dv_report.DvRp24Response;
import com.agribank.qldv_api.service.DVRecognitionService;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
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
    private final ModelMapper modelMapper;

    public PageResponse<DvRp24Response> search24(SearchRp24Request request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), getUserRequested()));

        PageResponse<DV> dvPageResponse = dvService.searchRp24(request);
        PageResponse<DvRp24Response> response = new PageResponse<>();
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setTotalItems(dvPageResponse.getTotalItems());
        if(Objects.isNull(dvPageResponse) || dvPageResponse.getData().isEmpty()){
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
    }}
