package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.request.casemgmt.CaseSearchRequest;
import com.agribank.qldv_api.request.casemgmt.EstablishmentCaseRequest;
import com.agribank.qldv_api.request.casemgmt.WorkflowActionRequest;
import com.agribank.qldv_api.response.casemgmt.CaseDetailResponse;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseDetailResponse;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.CaseService;
import com.agribank.qldv_api.service.EstablishmentCaseService;
import com.agribank.qldvutils.enums.EAuthorityLevel;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cases")
public class CaseController {
    private final CaseService caseService;
    private final EstablishmentCaseService establishmentCaseService;

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

    /** Xem chi tiết đầy đủ 15 field Bước 1 SC-02 — dùng chung cho cả cấp Agribank lẫn SC-07 sau này. */
    @RequirePermission(function = "FN1", action = "VIEW")
    @GetMapping("/{id}/establishment")
    public ResponseEntity<BaseResponse<EstablishmentCaseDetailResponse>> getEstablishmentDetail(@PathVariable String id) {
        return BaseResponse.success(establishmentCaseService.getEstablishmentCaseDetail(id));
    }

    /**
     * API-SC02-01 — luôn gọi service dùng chung với authorityLevel=BANK_LEVEL,
     * originFlow=CASE_FLOW_BTCDU ("A") và allowedOrganizationTypeId=null (client tự chọn trong
     * danh mục). SC-07 (Sprint 5, cấp cơ sở) sẽ có controller riêng gọi lại CÙNG service này với
     * authorityLevel/originFlow/allowedOrganizationTypeId khác — không sửa lại core logic (RR-03).
     */
    @RequirePermission(function = "FN1", action = "CREATE")
    @PostMapping("/establishments")
    public ResponseEntity<BaseResponse<EstablishmentCaseResponse>> createEstablishment(@RequestBody EstablishmentCaseRequest request) {
        request.validate();
        return BaseResponse.success(establishmentCaseService.createEstablishmentCase(
                request, EAuthorityLevel.BANK_LEVEL.getId(), Constants.CASE_FLOW_BTCDU, null));
    }

    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/establishment")
    public ResponseEntity<BaseResponse<EstablishmentCaseResponse>> updateEstablishment(@PathVariable String id, @RequestBody EstablishmentCaseRequest request) {
        request.validateForUpdate();
        return BaseResponse.success(establishmentCaseService.updateEstablishmentCase(id, request, null));
    }
}
