package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "partyOrganizationClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationClient {

    @PostMapping("api/v1/organization/save")
    DefaultResponse<Organization> save(
            @RequestBody Organization request
    );

    @PostMapping("api/v1/organization/save/all")
    DefaultResponse<List<Organization>> saveAll(
            @RequestBody List<Organization> requests
    );

    @GetMapping("api/v1/organization/find-by-id/{id}")
    DefaultResponse<Organization> findByCode(
            @PathVariable(name = "id") String id
    );

    @GetMapping("api/v1/organization/find-by-parent-code-max")
    DefaultResponse<Organization> getByParentCodeMax(
            @RequestParam(name = "parentCode") String parentCode
    );

    @PostMapping("api/v1/organization/search")
    DefaultResponse<PageResponse<Organization>> search(
            @RequestBody OrganizationSearchRequest request
    );

    @PostMapping("api/v1/organization/search-child")
    DefaultResponse<PageResponse<Organization>> searchChild(
            @RequestBody OrganizationSearchRequest request
    );

    @GetMapping("api/v1/organization/find-by-parent")
    DefaultResponse<List<Organization>> findByParent(
            @RequestParam(name = "code") String code
    );

    @GetMapping("api/v1/organization/find-by-parent-code")
    DefaultResponse<List<Organization>> findByParentCode(
            @RequestParam(name = "code") String code
    );

    @GetMapping("api/v1/organization/find-all-in-codes")
    BaseResponse<List<Organization>> findAllByCode(
            @RequestParam List<String> codeList
    );

    @GetMapping("api/v1/organization/find-by-user-id")
    DefaultResponse<Organization> findByUserId(
            @RequestParam(name = "userId") String userId
    );


    @GetMapping("api/v1/organization/advisory-agency")
    DefaultResponse<List<Organization>> advisoryAgency();

    @GetMapping("api/v1/organization/party-branch")
    DefaultResponse<List<Organization>> partyBranch();
}
