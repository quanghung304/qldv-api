package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.develop_plan.DevelopPlanDataRequest;
import com.agribank.qldv_api.request.develop_plan.DevelopPlanDetailUpdateRequest;
import com.agribank.qldv_api.request.develop_plan.GetDevelopmentPlanRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.develop_plan.DevelopPlanDetailResponse;
import com.agribank.qldv_api.service.development_plan.DevelopPlanDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/import")
    public ResponseEntity<DefaultResponse<String>> importExcel
            (@RequestParam(name = "file") MultipartFile file,
             @RequestParam(name = "organizationCode") String organizationCode,
             @RequestParam(name = "name") String name,
             @RequestParam(name = "start") Integer start,
             @RequestParam(name = "end") Integer end
             ) {
        return success(developPlanDetailService.importExcel(file, organizationCode, name, start, end), null);
    }

    @PostMapping("/update/import")
    public ResponseEntity<DefaultResponse<String>> importExcel
            (@RequestParam(name = "file") MultipartFile file,
             @RequestParam(name = "refId") String refId,
             @RequestParam(name = "organizationCode") String organizationCode,
             @RequestParam(name = "name") String name,
             @RequestParam(name = "start") Integer start,
             @RequestParam(name = "end") Integer end
            ) {
        return success(developPlanDetailService.importExcelUpdate(file, refId, organizationCode, name, start, end), null);
    }
}
