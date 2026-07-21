package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.casemgmt.CaseSearchRequest;
import com.agribank.qldv_api.request.casemgmt.WorkflowActionRequest;
import com.agribank.qldv_api.response.casemgmt.CaseDetailResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.CaseService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cases")
public class CaseController {
    private final CaseService caseService;

    @RequirePermission(function = "FN4", action = "VIEW")
    @PostMapping
    public ResponseEntity<BaseResponse<PageResponse<CaseListItemResponse>>> search(
            @RequestBody CaseSearchRequest request) {
        request.validate();
        return BaseResponse.success(caseService.search(request));
    }

    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CaseDetailResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(caseService.getById(id));
    }

    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/{id}/history")
    public ResponseEntity<BaseResponse<List<CaseHistoryItemResponse>>> getHistory(@PathVariable String id) {
        return BaseResponse.success(caseService.getHistory(id));
    }

    @RequirePermission(function = "FN2", action = "APPROVE")
    @PostMapping("/{id}/workflow-action")
    public ResponseEntity<BaseResponse<String>> workflowAction(@PathVariable String id,
                                                                 @RequestBody WorkflowActionRequest request) {
        request.validate();
        caseService.performWorkflowAction(id, request);
        return BaseResponse.success("Success");
    }
}
