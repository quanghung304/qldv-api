package com.agribank.qldv_api.gateway.development_plan;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "DevelopmentPlanDraftClient",
        url = "${qldv.database.url}" + "/api/v1/development-plan-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface DevelopmentPlanDraftClient extends BaseClient<DevelopmentPlanDraft, String> {
}
