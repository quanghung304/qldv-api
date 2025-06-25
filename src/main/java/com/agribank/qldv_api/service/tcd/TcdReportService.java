package com.agribank.qldv_api.service.tcd;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.tcd.Rp0304Response;
import com.agribank.qldv_api.response.tcd.Rp17Response;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.TransformationHistoryService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.form02.updown.TransformationHistory;
import com.agribank.qldvutils.request.form02.TransformationHistoryRpRequest;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TcdReportService {
    private final OrganizationService organizationService;
    private final TransformationHistoryService transformationHistoryService;
    private final ExportTcdReport03Service exportTcdReport03Service;
    private final DVService dvService;
    private final ExportTcdRp17Service exportTcdRp17Service;


    public PageResponse<Rp0304Response> search0304(OrganizationRpSearchRequest request){
        PageResponse<Rp0304Response> response = new PageResponse<>();

        PageResponse<Organization> organizationPageResponse = organizationService.searchRp(request);
        List<Organization> organizations = new ArrayList<>();
        if(Objects.isNull(organizationPageResponse) || organizationPageResponse.getData().isEmpty()){
            return response;
        }

        response.setCurrentPage(organizationPageResponse.getCurrentPage());
        response.setTotalPages(organizationPageResponse.getTotalPages());
        response.setTotalItems(organizationPageResponse.getTotalItems());

        List<Rp0304Response> rp0304Responses = new ArrayList<>();
        organizations = organizationPageResponse.getData();
        for(Organization organization : organizations){
            Rp0304Response rp0304Response = Rp0304Response.builder()
                    .code(organization.getCode())
                    .name(organization.getName())
                    .establishmentDate(organization.getEffectiveDate())
                    .form(organization.getForm())
                    .build();

            rp0304Responses.add(rp0304Response);
        }

        List<TransformationHistory> transformationHistories = getTransformationHistories(organizations, request);

        if (transformationHistories.isEmpty()) {
            response.setData(rp0304Responses);
            return response;
        }

        response.setData(makeResponse(transformationHistories, rp0304Responses));

        return response;
    }

    private List<TransformationHistory> getTransformationHistories(List<Organization> organizations, OrganizationRpSearchRequest request){
        List<String> organizationCode = organizations.stream().map(Organization::getCode).toList();
        TransformationHistoryRpRequest transformationHistoryRpRequest = TransformationHistoryRpRequest.builder()
                .codes(organizationCode)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();

        return transformationHistoryService.findByOrganizationCodeAndDate(transformationHistoryRpRequest);
    }

    private List<Rp0304Response> makeResponse( List<TransformationHistory> transformationHistories, List<Rp0304Response> rp0304Responses){
        Map<String, TransformationHistory> transformationHistoryMaps = new HashMap<>();
        for(TransformationHistory transformationHistory : transformationHistories){
            TransformationHistory transformationMapGet = transformationHistoryMaps
                    .getOrDefault(transformationHistory.getOrganizationCode()
                            + "_" + transformationHistory.getType(),
                            null);

            if (Objects.nonNull(transformationMapGet)
                    && transformationHistory.getCreatedAt()
                    .before(transformationMapGet.getCreatedAt())){
                transformationHistory = transformationMapGet;
            }

            transformationHistoryMaps.put(transformationHistory.getOrganizationCode()
                            + "_" + transformationHistory.getType(),
                    transformationHistory);
        }

        for (Rp0304Response rp0304Response : rp0304Responses){
            rp0304Response.setUpgradeDate(getDate(transformationHistoryMaps, rp0304Response, EReport01Type.UPGRADE.getId()));
            rp0304Response.setDowngradeDate(getDate(transformationHistoryMaps, rp0304Response, EReport01Type.DOWNGRADE.getId()));
        }
        return rp0304Responses;
    }

    private Date getDate(Map<String, TransformationHistory> transformationHistoryMaps, Rp0304Response rp0304Response, Integer type){
        TransformationHistory transformationHistory = transformationHistoryMaps
                .getOrDefault(rp0304Response.getCode()
                                + "_" + type,
                        null);

        if (Objects.nonNull(transformationHistory)){
            return transformationHistory.getEffectiveDate();
        }

        return null;
    }

    public ExportResponse exportExcelRp0304(OrganizationRpSearchRequest request) {
        try {
            return exportTcdReport03Service.exportData(request, EExcelColumnInfo.BC_03_04_DS.getName(), "", EExcelColumnInfo.BC_03_04_DS.name(), 1, EExcelColumnInfo.BC_03_04_DS.getName());
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }

    public PageResponse<Rp17Response> searchRp17(SearchRp17Request request) {
        return dvService.searchRp17(request);
    }


    public ExportResponse exportExcelRp17(SearchRp17Request request) {
        try {
            return exportTcdRp17Service.exportData(request, EExcelColumnInfo.BC_17_DSDV.getName(), "", EExcelColumnInfo.BC_17_DSDV.name(), 1, EExcelColumnInfo.BC_17_DSDV.getName());
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }
}
