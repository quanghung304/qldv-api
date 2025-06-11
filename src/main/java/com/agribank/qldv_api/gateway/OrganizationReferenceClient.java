package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.OrganizationReference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "organizationReferenceClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationReferenceClient {
    @GetMapping("api/v1/organization-reference/get-list-child")
    DefaultResponse<List<OrganizationReference>> getListChild(@RequestParam(name = "code") String code);

    @GetMapping("api/v1/organization-reference/find-by-id/{id}")
    DefaultResponse<OrganizationReference> findById(@PathVariable("id") String id);

    @GetMapping("api/v1/organization-reference/find-all")
    DefaultResponse<List<OrganizationReference>> findAll();
}
