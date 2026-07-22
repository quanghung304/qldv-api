package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.request.casemgmt.CaseSearchQuery;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "caseClient", url = "${qldv.database.url}" + "/api/v1/case", configuration = DatabaseFeignConfiguration.class)
public interface CaseClient extends BaseClient<Case, String> {
    @PostMapping("/search")
    BaseResponse<PageResponse<CaseListItemResponse>> search(@RequestBody CaseSearchQuery query);

    @GetMapping("/find-detail-by-id")
    BaseResponse<Optional<CaseListItemResponse>> findDetailById(@RequestParam String id);

    @GetMapping("/exists-by-case-code")
    BaseResponse<Boolean> existsByCaseCode(@RequestParam String caseCode);

    @GetMapping("/find-latest-by-prefix")
    BaseResponse<Optional<Case>> findLatestByPrefix(@RequestParam String prefix);
}
