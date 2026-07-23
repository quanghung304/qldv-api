package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.CaseBoardReview;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@FeignClient(name = "caseBoardReviewClient", url = "${qldv.database.url}" + "/api/v1/case-board-review",
        configuration = DatabaseFeignConfiguration.class)
public interface CaseBoardReviewClient {
    @PostMapping("/upsert")
    BaseResponse<CaseBoardReview> upsert(@RequestBody CaseBoardReview request);

    @GetMapping("/find-by-case-id")
    BaseResponse<Optional<CaseBoardReview>> findByCaseId(@RequestParam String caseId);
}
