package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseChangeTarget;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "caseChangeTargetClient", url = "${qldv.database.url}" + "/api/v1/case-change-target", configuration = DatabaseFeignConfiguration.class)
public interface CaseChangeTargetClient extends BaseClient<CaseChangeTarget, String> {
    @GetMapping("/find-by-case-id")
    DefaultListResponse<CaseChangeTarget> findByCaseId(@RequestParam String caseId);
}
