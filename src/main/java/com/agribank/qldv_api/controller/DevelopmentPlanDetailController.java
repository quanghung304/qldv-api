package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.develop_plan.DevelopPlanDataRequest;
import com.agribank.qldv_api.request.develop_plan.DevelopPlanDetailUpdateRequest;
import com.agribank.qldv_api.request.develop_plan.GetDevelopmentPlanRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanDetailResponse;
import com.agribank.qldv_api.service.development_plan.DevelopPlanDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.agribank.qldv_api.response.DefaultResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/develop-plan-detail")
public class DevelopmentPlanDetailController {
    private final DevelopPlanDetailService developPlanDetailService;

    @PostMapping("/get-detail")
    public ResponseEntity<DefaultListResponse<DevelopPlanDetailResponse>> getPlanDetail(@RequestBody GetDevelopmentPlanRequest request) {
        request.validate();
        return com.agribank.qldv_api.response.DefaultListResponse.success(developPlanDetailService.getPlanDetail(request));
    }

    @PostMapping("/add")
    public ResponseEntity<DefaultResponse<String>> addDevelopPlan(@RequestBody DevelopPlanDataRequest request) {
        request.validate();
        return success(developPlanDetailService.addDevelopPlanDetail(request));
    }

    @PutMapping("/update")
    public ResponseEntity<DefaultResponse<String>> updateDevelopPlan(@RequestBody DevelopPlanDetailUpdateRequest request) {
        request.validate();
        return success(developPlanDetailService.updateDevelopPlanDetail(request), null);
    }

    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<DefaultResponse<String>> delete(@PathVariable(name = "id") String id) {
        return success(developPlanDetailService.createRequestDelete(id), null);
    }
}
