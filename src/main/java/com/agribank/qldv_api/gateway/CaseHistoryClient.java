package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseHistory;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "caseHistoryClient", url = "${qldv.database.url}" + "/api/v1/case-history", configuration = DatabaseFeignConfiguration.class)
public interface CaseHistoryClient extends BaseClient<CaseHistory, String> {
}
