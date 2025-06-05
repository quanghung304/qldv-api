package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.OrganizationDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "organizationDraftClient", url = "${qldv.database.url}" + "/api/v1/organization-draft", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationDraftClient extends BaseClient<OrganizationDraft, String>{
}
