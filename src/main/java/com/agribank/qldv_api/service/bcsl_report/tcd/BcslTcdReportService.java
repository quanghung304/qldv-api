package com.agribank.qldv_api.service.bcsl_report.tcd;

import com.agribank.qldv_api.enums.EOrganizationReference;
import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp09Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp02Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BcslTcdReportService {
    private final DVClient dvClient;
    private final OrganizationService organizationService;
    private final OrganizationClient organizationClient;

    public PageResponse<BcslTcdRp01Response> searchRp01(SearchRp01Request request) {
        if (Objects.isNull(request.getOrderBy())) {
            request.setOrderBy("rootCode");
        }
        PageResponse<BcslTcdRp01Response> response = organizationService.searchRp01(request);

        if (Objects.isNull(response.getData()) || response.getData().isEmpty()) {
            return response;
        }

        List<BcslTcdRp01Response> data = response.getData();
        data.add(countTotal(data));
        response.setData(data);

        if (!EOrganizationReference.GROUP_A.getCode().contains(request.getForm())) {
            return response;
        }

        return countFormA(response);
    }

    private PageResponse<BcslTcdRp01Response> countFormA(PageResponse<BcslTcdRp01Response> response){
        List<Organization> organizations = organizationService.getActiveOrganizations();

        if (organizations.isEmpty()) {
            return response;
        }

        BcslTcdRp01Response bcslRp01Response = BcslTcdRp01Response.builder()
                .form("A")
                .b1(0)
                .b2(0)
                .b3(0)
                .c1(0)
                .c2(0)
                .d(0)
                .build();
        for (Organization organization : organizations) {
            switch (organization.getForm()) {
                case "B1":
                    bcslRp01Response.setB1(bcslRp01Response.getB1() + 1);
                    break;
                case "B2":
                    bcslRp01Response.setB2(bcslRp01Response.getB2() + 1);
                    break;
                case "B3":
                    bcslRp01Response.setB3(bcslRp01Response.getB3() + 1);
                    break;
                case "C1":
                    bcslRp01Response.setC1(bcslRp01Response.getC1() + 1);
                    break;
                case "C2":
                    bcslRp01Response.setC2(bcslRp01Response.getC2() + 1);
                    break;
                case "D":
                    bcslRp01Response.setD(bcslRp01Response.getD() + 1);
                    break;
                default:
                    break;
            }
        }

        for (BcslTcdRp01Response tcd : response.getData()){
            if (EOrganizationReference.GROUP_A.getCode().contains(tcd.getForm())) {
                tcd.setB1(bcslRp01Response.getB1());
                tcd.setB2(bcslRp01Response.getB2());
                tcd.setB3(bcslRp01Response.getB3());
                tcd.setC1(bcslRp01Response.getC1());
                tcd.setC2(bcslRp01Response.getC2());
                tcd.setD(bcslRp01Response.getD());
            }
        }
        return response;
    }

    private BcslTcdRp01Response countTotal(List<BcslTcdRp01Response> tcdRp01Responses){
        BcslTcdRp01Response bcslRp01Response = BcslTcdRp01Response.builder()
                .form("Total")
                .b1(0)
                .b2(0)
                .b3(0)
                .c1(0)
                .c2(0)
                .d(0)
                .build();

        for (BcslTcdRp01Response tcd : tcdRp01Responses){
            if (EOrganizationReference.GROUP_A.getCode().contains(tcd.getForm())) {
                continue;
            }
            Integer b1 = bcslRp01Response.getB1() + tcd.getB1();
            bcslRp01Response.setB1(b1);

            Integer b2 = bcslRp01Response.getB2() + tcd.getB2();
            bcslRp01Response.setB2(b2);

            Integer b3 = bcslRp01Response.getB3() + tcd.getB3();
            bcslRp01Response.setB3(b3);

            Integer c1 = bcslRp01Response.getC1() + tcd.getC1();
            bcslRp01Response.setC1(c1);

            Integer c2 = bcslRp01Response.getC2() + tcd.getC2();
            bcslRp01Response.setC2(c2);

            Integer d = bcslRp01Response.getD() + tcd.getD();
            bcslRp01Response.setD(d);
        }
        return bcslRp01Response;
    }

    public PageResponse<BcslTcdRp09Response> searchRp09(SearchRpRequest request){
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(),getUserRequested()));
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("code");
        }

        return dvClient.searchRp09(request).getData();
    }

    public UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public PageResponse<BcslTcdRp02Response> searchRp02(SearchRequest request) {
        String organizationCode = Objects.nonNull(request.getOrganizationCode()) ? request.getOrganizationCode() : CommonUtils.getOrganizationByRequestedUser();
        request.setOrganizationCode(organizationCode);
        return organizationClient.searchRp02(request).getData();
    }
}
