package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseHistory;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "caseHistoryClient", url = "${qldv.database.url}" + "/api/v1/case-history", configuration = DatabaseFeignConfiguration.class)
public interface CaseHistoryClient extends BaseClient<CaseHistory, String> {
    @GetMapping("/find-by-case-id")
    DefaultListResponse<CaseHistoryItemResponse> findByCaseId(@RequestParam String caseId);
}
