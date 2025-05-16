package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.developPlan.DevelopPlanDataRequest;
import com.agribank.qldv_api.request.developPlan.DevelopPlanDetailUpdateRequest;
import com.agribank.qldv_api.request.developPlan.GetDevelopmentPlanRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanDetailResponse;
import com.agribank.qldv_api.service.developPlan.DevelopPlanDetailService;
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
        return success(developPlanDetailService.updateDevelopPlanDetail(request));
    }
}
