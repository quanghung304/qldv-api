package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.organization.OrganizationSearchQuery;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.organization.OrganizationChildCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "organizationClient", url = "${qldv.database.url}" + "/api/v1/organization", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationClient extends BaseClient<Organization, String> {
    @PostMapping("/search")
    BaseResponse<PageResponse<Organization>> search(@RequestBody OrganizationSearchQuery query);

    @GetMapping("/find-descendant-ids")
    DefaultListResponse<String> findDescendantIds(@RequestParam String rootId);

    @GetMapping("/count-children-by-parent-ids")
    DefaultListResponse<OrganizationChildCountResponse> countChildrenByParentIds(@RequestParam List<String> parentIds);

    @GetMapping("/exists-active-by-name")
    BaseResponse<Boolean> existsActiveByName(@RequestParam String organizationName, @RequestParam Integer operationStatus);
}
