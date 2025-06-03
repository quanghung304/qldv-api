package com.agribank.qldv_api.gateway.development_plan;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetail;
import com.agribank.qldvutils.request.development_plan.DevelopDetailRefIdRequest;
import com.agribank.qldvutils.request.development_plan.DevelopPlanUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "development-plan-detail",
        url = "${qldv.database.url}" + "/api/v1/development-plan-detail",
        configuration = DatabaseFeignConfiguration.class)
public interface DevelopmentPlanDetailClient extends BaseClient<DevelopmentPlanDetail, String> {
    @PostMapping("/get-by-refid")
    DefaultResponse<List<DevelopmentPlanDetail>> getByRefId(
            @RequestParam String refId
    );

    @DeleteMapping("api/v1/development-plan-detail/delete-by-ref-id")
    DefaultResponse<DevelopPlanUpdateRequest> deleteByRefId(
            @RequestBody DevelopPlanUpdateRequest request
    );

    @PostMapping("/find-by-ref-id-and-year")
    DefaultResponse<List<DevelopmentPlanDetail>> findByRefIdAndYearIn(
            @RequestBody DevelopDetailRefIdRequest request
    );
}
