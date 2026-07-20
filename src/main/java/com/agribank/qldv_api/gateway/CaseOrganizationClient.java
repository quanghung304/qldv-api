package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseOrganization;
import com.agribank.qldvutils.entity.CaseOrganizationId;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "caseOrganizationClient", url = "${qldv.database.url}" + "/api/v1/case-organization", configuration = DatabaseFeignConfiguration.class)
public interface CaseOrganizationClient extends BaseClient<CaseOrganization, CaseOrganizationId> {
    @GetMapping("/find-with-organization-by-case-id")
    DefaultListResponse<CaseOrganizationSummaryResponse> findWithOrganizationByCaseId(@RequestParam String caseId);
}
