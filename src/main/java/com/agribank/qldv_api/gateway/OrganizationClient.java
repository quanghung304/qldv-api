package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.organization.OrganizationSearchQuery;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.organization.OrganizationChildCountResponse;
import com.agribank.qldvutils.response.organization.OrganizationSubordinateRawResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{organizationId}/subordinates")
    DefaultListResponse<OrganizationSubordinateRawResponse> findSubordinates(@PathVariable String organizationId);

    /** API-SC06-02 — sinh organization_code sequential-per-prefix, cùng kiểu Case.generateCaseCode(). */
    @GetMapping("/find-latest-by-prefix")
    BaseResponse<Optional<Organization>> findLatestByPrefix(@RequestParam String prefix);
}
