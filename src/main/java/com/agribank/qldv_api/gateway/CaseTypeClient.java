package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseType;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "caseTypeClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface CaseTypeClient extends BaseClient<CaseType, String> {
}
