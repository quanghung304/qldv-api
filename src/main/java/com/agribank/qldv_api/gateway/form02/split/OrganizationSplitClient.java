package com.agribank.qldv_api.gateway.form02.split;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplit;
import com.agribank.qldvutils.request.form02.SearchOrganizationSplitRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "organizationSplitClient", url = "${qldv.database.url}" + "/api/v1/split", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationSplitClient extends BaseClient<OrganizationSplit, String> {
    @PostMapping("/search")
    BaseResponse<PageResponse<OrganizationSplit>> search(
            @RequestBody SearchOrganizationSplitRequest request
    );
}
