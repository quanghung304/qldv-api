package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "organizationSplitDraftClient",
        url = "${qldv.database.url}" + "/api/v1/split-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationSplitDraftClient extends BaseClient<OrganizationSplitDraft, String> {
    @GetMapping("/pending/{code}")
    BaseResponse<OrganizationSplitDraft> findPendingRequest(@PathVariable String code);
}
