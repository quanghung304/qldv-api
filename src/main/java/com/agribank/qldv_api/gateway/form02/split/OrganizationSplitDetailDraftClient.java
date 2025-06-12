package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetailDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "organizationSplitDetailDraftClient",
        url = "${qldv.database.url}" + "/api/v1/split-detail-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationSplitDetailDraftClient extends BaseClient<OrganizationSplitDetailDraft, String> {
    @GetMapping("find-by-split-id/{splitId}")
    DefaultListResponse<OrganizationSplitDetailDraft> findBySplitId(@PathVariable String splitId);
}
