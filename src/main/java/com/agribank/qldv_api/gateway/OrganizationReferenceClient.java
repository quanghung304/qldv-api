package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.OrganizationReference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "organizationReferenceClient", url = "${qldv.database.url}" + "/api/v1/organization-reference", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationReferenceClient extends BaseClient<OrganizationReference, String> {
    @GetMapping("/get-list-child")
    DefaultResponse<List<OrganizationReference>> getListChild(@RequestParam(name = "code") String code);
}
