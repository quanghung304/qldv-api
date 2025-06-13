package com.agribank.qldv_api.gateway.form02.merge;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetail;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMergeDetailDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "organizationMergeDetailClient",
        url = "${qldv.database.url}" + "/api/v1/merge-detail",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationMergeDetailClient extends BaseClient<OrganizationMergeDetail, String> {
    @GetMapping("find-by-ref/{refId}")
    DefaultListResponse<OrganizationMergeDetail> findByRefId(@PathVariable String refId);
}
