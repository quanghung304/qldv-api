package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.DevelopmentPlan;
import com.agribank.qldvutils.request.developPlan.DevelopPrntBrcdRequest;
import com.agribank.qldvutils.request.developPlan.GetChildPlanRequest;
import com.agribank.qldvutils.request.developPlan.GetPlanRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "development-plan", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DevelopmentPlanClient {
    @PostMapping("api/v1/development-plan/save")
    DefaultResponse<DevelopmentPlan> savePlan(
            @RequestBody DevelopmentPlan plan
    );
    @PostMapping("api/v1/development-plan/search-prnt-brcd")
    DefaultResponse<DevelopmentPlan> searchPrntBrcd(
            @RequestBody DevelopPrntBrcdRequest request
    );
    @PostMapping("api/v1/development-plan/get-plan")
    DefaultResponse<PageResponse<DevelopmentPlan>> getPlan(
            @RequestBody GetPlanRequest request
    );
    @PostMapping("api/v1/development-plan/get-child-plan")
    DefaultResponse<List<DevelopmentPlan>> getChildPlan(
            @RequestBody GetChildPlanRequest request
    );
}
