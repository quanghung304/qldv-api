package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.developPlan.DevelopPlanRequest;
import com.agribank.qldv_api.request.developPlan.GetDevelopmentPlanRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.developPlan.DevelopPlanResponse;
import com.agribank.qldv_api.service.developPlan.DevelopPlanService;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.agribank.qldv_api.response.DefaultResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/develop-plan")
public class DevelopmentPlanController {
    private final DevelopPlanService developPlanService;

    @PostMapping("/get-plan")
    public ResponseEntity<DefaultResponse<PageResponse<DevelopPlanResponse>>> getPlan(@RequestBody GetDevelopmentPlanRequest request) {
        request.validate();
        return success(developPlanService.getPlan(request));
    }

    @PostMapping("/get-child-plan")
    public ResponseEntity<DefaultListResponse<DevelopPlanResponse>> getChildPlan(@RequestBody GetDevelopmentPlanRequest request) {
        request.validate();
        return com.agribank.qldv_api.response.DefaultListResponse.success(developPlanService.getChildPlan(request));
    }

    @PostMapping("/add")
    public ResponseEntity<DefaultResponse<String>> addDevelopPlan(@RequestBody DevelopPlanRequest request) {
        request.validate();
        return success(developPlanService.addDevelopPlan(request));
    }
}
