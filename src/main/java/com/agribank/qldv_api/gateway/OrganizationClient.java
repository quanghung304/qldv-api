package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
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

    @PostMapping("api/v1/organization/search-rp")
    DefaultResponse<PageResponse<Organization>> searchRp(
            @RequestBody OrganizationRpSearchRequest request
    );

    @GetMapping("api/v1/organization/all-parent")
    DefaultResponse<List<Organization>> getOrganizationAllParent(@RequestParam(name = "code") String code);

    @GetMapping("api/v1/organization/find-all")
    DefaultResponse<List<Organization>> findAll();

    @GetMapping("api/v1/organization/form-b")
    DefaultResponse<List<Organization>> getOrganizationFormB();

    @DeleteMapping("api/v1/organization/delete-by-id/{id}")
    DefaultResponse<Organization> deleteById(@PathVariable("id") String id);

    @DeleteMapping("api/v1/organization/delete-all-by-id")
    DefaultResponse<Organization> deleteAllById(@RequestBody List<String> ids);

    @GetMapping("api/v1/organization/active-organizations")
    DefaultResponse<List<Organization>> getActiveOrganizations();

    @PostMapping("api/v1/organization/search-rp-01")
    DefaultResponse<PageResponse<BcslTcdRp01Response>> searchRp01(@RequestBody SearchRp01Request request);
}
