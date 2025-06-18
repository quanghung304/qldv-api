package com.agribank.qldv_api.gateway.development_plan;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlan;
import com.agribank.qldvutils.request.development_plan.DevelopPrntBrcdRequest;
import com.agribank.qldvutils.request.development_plan.GetChildPlanRequest;
import com.agribank.qldvutils.request.development_plan.GetPlanRequest;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "development-plan",
        url = "${qldv.database.url}" + "/api/v1/development-plan",
        configuration = DatabaseFeignConfiguration.class)
public interface DevelopmentPlanClient extends BaseClient<DevelopmentPlan, String> {

    @PostMapping("/search-prnt-brcd")
    DefaultResponse<DevelopmentPlan> searchPrntBrcd(
            @RequestBody DevelopPrntBrcdRequest request
    );
    @PostMapping("/get-plan")
    DefaultResponse<PageResponse<DevelopmentPlan>> getPlan(
            @RequestBody GetPlanRequest request
    );
    @PostMapping("/get-child-plan")
    DefaultResponse<List<DevelopmentPlan>> getChildPlan(
            @RequestBody GetChildPlanRequest request
    );

    @GetMapping("/find-by-organization-code")
    DefaultResponse<DevelopmentPlan> findByOrganizationCode(
            @RequestParam(name = "organizationCode") String organizationCode
    );

    @GetMapping("/find-by-organization-code")
    DefaultListResponse<DevelopmentPlan> findByOrganizationCodeIn(
            @RequestBody List<String> organizationCodes
    );
}
