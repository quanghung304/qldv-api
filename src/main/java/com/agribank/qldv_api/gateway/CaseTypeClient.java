package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "caseTypeClient", url = "${qldv.database.url}" + "/api/v1/case-type", configuration = DatabaseFeignConfiguration.class)
public interface CaseTypeClient extends BaseClient<CaseType, String> {
    @GetMapping("/find-by-code")
    BaseResponse<Optional<CaseType>> findByCode(@RequestParam String code);
}
