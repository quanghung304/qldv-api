package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.OrganizationType;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "organizationTypeClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationTypeClient extends BaseClient<OrganizationType, String> {
}
