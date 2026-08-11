package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "organizationTypeClient", url = "${qldv.database.url}" + "/api/v1/organization-type", configuration = DatabaseFeignConfiguration.class)
public interface OrganizationTypeClient extends BaseClient<OrganizationType, String> {
    @GetMapping("/find-by-code")
    BaseResponse<Optional<OrganizationType>> findByCode(@RequestParam String code);
}
