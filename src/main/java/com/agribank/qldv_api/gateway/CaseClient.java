package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Case;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "caseClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface CaseClient extends BaseClient<Case, String> {
}
