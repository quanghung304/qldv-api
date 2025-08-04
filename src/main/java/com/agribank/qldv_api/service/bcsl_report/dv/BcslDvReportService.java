package com.agribank.qldv_api.service.bcsl_report.dv;

import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.DvRp18Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class BcslDvReportService {
    private final DVService dvService;
    private final OrganizationService organizationService;

    public PageResponse<DvRp18Response> searchRp18(SearchRequest request) {
        request.validate();
        UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        request.setOrganizationCode(organizationService.getOrganizationCode(request.getOrganizationCode(), userRequested));

        if (Objects.isNull(request.getOrganizationCode())) {
            request.setOrganizationCode("1000");
        }

        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("organizationCode");
        }

        return dvService.searchRp18(request);
    }
}
