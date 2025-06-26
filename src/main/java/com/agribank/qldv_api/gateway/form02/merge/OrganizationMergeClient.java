package com.agribank.qldv_api.gateway.form02.merge;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "organizationMergeClient",
        url = "${qldv.database.url}" + "/api/v1/merge",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationMergeClient extends BaseClient<OrganizationMerge, String> {
    @PostMapping("/search/union")
    BaseResponse<PageResponse<OrganizationMerge>> searchUnion(
            @RequestBody SearchOrganizationUnionRequest request
    );

}
