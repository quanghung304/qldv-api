package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.DevelopmentPlanDetail;
import com.agribank.qldvutils.request.developPlan.DevelopPlanUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "development-plan-detail", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DevelopmentPlanDetailClient {
    @PostMapping("api/v1/development-plan-detail/get-by-refid")
    DefaultResponse<List<DevelopmentPlanDetail>> getByRefId(
            @RequestParam String refId
    );
    @PostMapping("api/v1/development-plan-detail/save")
    DefaultResponse<List<DevelopmentPlanDetail>> saveDetail(
            @RequestBody List<DevelopmentPlanDetail> detail
    );
    @DeleteMapping("api/v1/development-plan-detail/delete")
    DefaultResponse<DevelopPlanUpdateRequest> deleteById(
            @RequestBody DevelopPlanUpdateRequest request
    );
}
