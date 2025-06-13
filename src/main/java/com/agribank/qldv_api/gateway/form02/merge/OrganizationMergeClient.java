package com.agribank.qldv_api.gateway.form02.merge;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.request.PagingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "organizationMergeClient",
        url = "${qldv.database.url}" + "/api/v1/merge",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationMergeClient extends BaseClient<OrganizationMerge, String> {
    @PostMapping("list")
    DefaultListResponse<OrganizationMerge> getList(
            @RequestBody PagingRequest request
    );

}
