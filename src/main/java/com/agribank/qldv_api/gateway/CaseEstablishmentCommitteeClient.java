package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CaseEstablishmentCommitteeId;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "caseEstablishmentCommitteeClient", url = "${qldv.database.url}" + "/api/v1/case-establishment-committee", configuration = DatabaseFeignConfiguration.class)
public interface CaseEstablishmentCommitteeClient extends BaseClient<CaseEstablishmentCommittee, CaseEstablishmentCommitteeId> {
    @GetMapping("/find-by-case-id")
    DefaultListResponse<CaseEstablishmentCommittee> findByCaseId(@RequestParam String caseId);

    @DeleteMapping("/delete-by-case-id")
    BaseResponse<String> deleteByCaseId(@RequestParam String caseId);
}
