package com.agribank.qldv_api.gateway.form02.merge;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "OrganizationMergeDraftClient",
        url = "${qldv.database.url}" + "/api/v1/merge-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationMergeDraftClient extends BaseClient<OrganizationMergeDraft, String> {
}
