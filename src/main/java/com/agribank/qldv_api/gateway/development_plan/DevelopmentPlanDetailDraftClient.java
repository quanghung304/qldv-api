package com.agribank.qldv_api.gateway.development_plan;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetailDraft;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "DevelopmentPlanDetailDraftClient",
        url = "${qldv.database.url}" + "/api/v1/development-plan-detail-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface DevelopmentPlanDetailDraftClient extends BaseClient<DevelopmentPlanDetailDraft, String> {

    @GetMapping("/find-by-ref-id")
    DefaultResponse<List<DevelopmentPlanDetailDraft>> findByRefId(@RequestParam(name = "refId") String refId);
}
