package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetail;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "organizationSplitDetailClient",
        url = "${qldv.database.url}" + "/api/v1/split-detail",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationSplitDetailClient extends BaseClient<OrganizationSplitDetail, String> {
    @GetMapping("find-by-ref/{refId}")
    DefaultListResponse<OrganizationSplitDetail> findBySplitId(@PathVariable String refId);
}