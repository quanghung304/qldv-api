package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Organization;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "organizationClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationClient extends BaseClient<Organization, String> {
}
