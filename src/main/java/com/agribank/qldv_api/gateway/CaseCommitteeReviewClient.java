package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseCommitteeReview;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@FeignClient(name = "caseCommitteeReviewClient", url = "${qldv.database.url}" + "/api/v1/case-committee-review",
        configuration = DatabaseFeignConfiguration.class)
public interface CaseCommitteeReviewClient {
    @PostMapping("/upsert")
    BaseResponse<CaseCommitteeReview> upsert(@RequestBody CaseCommitteeReview request);

    @GetMapping("/find-by-case-id")
    BaseResponse<Optional<CaseCommitteeReview>> findByCaseId(@RequestParam String caseId);
}
