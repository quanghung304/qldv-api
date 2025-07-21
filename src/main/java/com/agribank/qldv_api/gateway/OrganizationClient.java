package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.organization.ApproveOrganizationRequest;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "partyOrganizationClient", url = "${qldv.database.url}" + "/api/v1/organization", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationClient extends BaseClient<Organization, String> {
    @GetMapping("/find-by-id/{id}")
    DefaultResponse<Organization> findByCode(
            @PathVariable(name = "id") String id
    );

    @GetMapping("/find-by-parent-code-max")
    DefaultResponse<Organization> getByParentCodeMax(
            @RequestParam(name = "parentCode") String parentCode
    );

    @PostMapping("/search")
    DefaultResponse<PageResponse<Organization>> search(
            @RequestBody OrganizationSearchRequest request
    );

    @PostMapping("/search-child")
    DefaultResponse<PageResponse<Organization>> searchChild(
            @RequestBody OrganizationSearchRequest request
    );

    @GetMapping("/find-by-parent")
    DefaultResponse<List<Organization>> findByParent(
            @RequestParam(name = "code") String code
    );

    @GetMapping("/find-by-parent-code")
    DefaultResponse<List<Organization>> findByParentCode(
            @RequestParam(name = "code") String code
    );

    @GetMapping("/find-all-in-codes")
    BaseResponse<List<Organization>> findAllByCode(
            @RequestParam List<String> codeList
    );

    @GetMapping("/find-by-user-id")
    DefaultResponse<Organization> findByUserId(
            @RequestParam(name = "userId") String userId
    );

    @GetMapping("/advisory-agency")
    DefaultResponse<List<Organization>> advisoryAgency();

    @GetMapping("/party-branch")
    DefaultResponse<List<Organization>> partyBranch();

    @PostMapping("/search-rp")
    DefaultResponse<PageResponse<Organization>> searchRp(
            @RequestBody OrganizationRpSearchRequest request
    );

    @GetMapping("/all-parent")
    DefaultResponse<List<Organization>> getOrganizationAllParent(@RequestParam(name = "code") String code);
    

    @GetMapping("/form-b")
    DefaultResponse<List<Organization>> getOrganizationFormB();

    @DeleteMapping("/delete-all-by-id")
    DefaultResponse<Organization> deleteAllById(@RequestBody List<String> ids);

    @GetMapping("/active-organizations")
    DefaultResponse<List<Organization>> getActiveOrganizations();

    @PostMapping("/search-rp-01")
    DefaultResponse<PageResponse<BcslTcdRp01Response>> searchRp01(@RequestBody SearchRp01Request request);

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody ApproveOrganizationRequest request);

    @GetMapping("/children-max")
    BaseResponse<Organization> getChildrenMax(@RequestParam(name = "code") String code,
                                              @RequestParam(name = "form") String form);

}
