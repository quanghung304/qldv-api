package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.request.casemgmt.EstablishmentCasePersistRequest;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "caseEstablishmentClient", url = "${qldv.database.url}" + "/api/v1/case-establishment", configuration = DatabaseFeignConfiguration.class)
public interface CaseEstablishmentClient extends BaseClient<CaseEstablishment, String> {
    /** Ghi Case + CaseEstablishment + committee + liên kết attachment trong 1 transaction ở qldv-db. */
    @PostMapping("/persist")
    BaseResponse<Case> persist(@RequestBody EstablishmentCasePersistRequest request);

    /** case_id KHÔNG phải PK của CaseEstablishment (PK là id UUID riêng) — KHÔNG dùng findById(caseId). */
    @GetMapping("/find-by-case-id")
    BaseResponse<Optional<CaseEstablishment>> findByCaseId(@RequestParam String caseId);
}
