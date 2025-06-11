package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplit;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "organizationSplitClient", url = "${qldv.database.url}" + "/api/v1/split", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationSplitClient extends BaseClient<OrganizationSplit, String> {
}
