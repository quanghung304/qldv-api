package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetail;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "organizationSplitDetailClient",
        url = "${qldv.database.url}" + "/api/v1/split-detail",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationSplitDetailClient extends BaseClient<OrganizationSplitDetail, String> {
}
