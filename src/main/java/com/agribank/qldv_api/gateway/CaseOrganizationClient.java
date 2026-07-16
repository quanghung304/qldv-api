package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseOrganization;
import com.agribank.qldvutils.entity.CaseOrganizationId;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "caseOrganizationClient", url = "${qldv.database.url}" + "/api/v1/case-organization", configuration = DatabaseFeignConfiguration.class)
public interface CaseOrganizationClient extends BaseClient<CaseOrganization, CaseOrganizationId> {
}
