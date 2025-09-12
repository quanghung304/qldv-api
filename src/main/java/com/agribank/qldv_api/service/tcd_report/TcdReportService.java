package com.agribank.qldv_api.service.tcd_report;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.response.tcd.Rp0304Response;
import com.agribank.qldv_api.service.form02.OrganizationUpDownService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.Report05BcdsDto;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDown;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.form02.OrganizationUpDownRpRequest;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TcdReportService {
    private final OrganizationService organizationService;
    private final OrganizationUpDownService OrganizationUpDownService;
    private final ExportTcdReport03Service exportTcdReport03Service;
    private final OrganizationHistoryClient historyClient;

    public PageResponse<Rp0304Response> search0304(OrganizationRpSearchRequest request){
        PageResponse<Rp0304Response> response = new PageResponse<>();

        PageResponse<Organization> organizationPageResponse = organizationService.searchRp(request);
        List<Organization> organizations;
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

        List<OrganizationUpDown> transformationHistories = getTransformationHistories(organizations, request);

        if (transformationHistories.isEmpty()) {
            response.setData(rp0304Responses);
            return response;
        }

        response.setData(makeResponse(transformationHistories, rp0304Responses));

        return response;
    }

    private List<OrganizationUpDown> getTransformationHistories(List<Organization> organizations, OrganizationRpSearchRequest request){
        List<String> organizationCode = organizations.stream().map(Organization::getCode).toList();
        OrganizationUpDownRpRequest organizationUpDownRpRequest = OrganizationUpDownRpRequest.builder()
                .codes(organizationCode)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();

        return OrganizationUpDownService.findByOrganizationCodeAndDate(organizationUpDownRpRequest);
    }

    private List<Rp0304Response> makeResponse( List<OrganizationUpDown> transformationHistories, List<Rp0304Response> rp0304Responses){
        Map<String, OrganizationUpDown> OrganizationUpDownMaps = new HashMap<>();
        for(OrganizationUpDown OrganizationUpDown : transformationHistories){
            OrganizationUpDown transformationMapGet = OrganizationUpDownMaps
                    .getOrDefault(OrganizationUpDown.getOrganizationCode()
                            + "_" + OrganizationUpDown.getType(),
                            null);

            if (Objects.nonNull(transformationMapGet)
                    && OrganizationUpDown.getCreatedAt()
                    .before(transformationMapGet.getCreatedAt())){
                OrganizationUpDown = transformationMapGet;
            }

            OrganizationUpDownMaps.put(OrganizationUpDown.getOrganizationCode()
                            + "_" + OrganizationUpDown.getType(),
                    OrganizationUpDown);
        }

        for (Rp0304Response rp0304Response : rp0304Responses){
            rp0304Response.setUpgradeDate(getDate(OrganizationUpDownMaps, rp0304Response, EReport01Type.UPGRADE.getId()));
            rp0304Response.setDowngradeDate(getDate(OrganizationUpDownMaps, rp0304Response, EReport01Type.DOWNGRADE.getId()));
        }
        return rp0304Responses;
    }

    private Date getDate(Map<String, OrganizationUpDown> OrganizationUpDownMaps, Rp0304Response rp0304Response, Integer type){
        OrganizationUpDown OrganizationUpDown = OrganizationUpDownMaps
                .getOrDefault(rp0304Response.getCode()
                                + "_" + type,
                        null);

        if (Objects.nonNull(OrganizationUpDown)){
            return OrganizationUpDown.getEffectiveDate();
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


    public PDFContentResult exportPDFRp0304(OrganizationRpSearchRequest request) {
        try {
            return exportTcdReport03Service.exportPDFData(request, EExcelColumnInfo.BC_03_04_DS.getName(), "", EExcelColumnInfo.BC_03_04_DS.getName(), 1, EExcelColumnInfo.BC_03_04_DS.getPageType());
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return null;
    }


    public PageResponse<Report05BcdsDto> searchRp05(SearchRpRequest request) {
        String organizationCode = Objects.nonNull(request.getOrganizationCode()) ? request.getOrganizationCode() : CommonUtils.getOrganizationByRequestedUser();
        request.setOrganizationCode(organizationCode);
        return historyClient.searchRp05(request).getData();
    }
}
