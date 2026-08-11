package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseChange;
import com.agribank.qldvutils.request.casemgmt.CaseChangePersistRequest;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "caseChangeClient", url = "${qldv.database.url}" + "/api/v1/case-change", configuration = DatabaseFeignConfiguration.class)
public interface CaseChangeClient extends BaseClient<CaseChange, String> {
    /** API-SC08-01/02 — persist PMDV_CASE + PMDV_CASE_CHANGE + PMDV_CASE_ORGANIZATION trong 1 transaction (qldv-db). */
    @PostMapping("/persist")
    BaseResponse<Case> persist(@RequestBody CaseChangePersistRequest request);

    @GetMapping("/find-by-case-id")
    BaseResponse<Optional<CaseChange>> findByCaseId(@RequestParam String caseId);
}
