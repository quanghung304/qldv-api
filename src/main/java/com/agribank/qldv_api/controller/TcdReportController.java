package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.tcd.Rp0304Response;
import com.agribank.qldv_api.service.tcd.TcdReportService;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tcd-report")
public class TcdReportController {
    private final TcdReportService service;

    @PostMapping("/search/03-04")
    public ResponseEntity<BaseResponse<PageResponse<Rp0304Response>>> search(@RequestBody @Valid OrganizationRpSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search0304(request));
    }

    @PostMapping("/export/03-04")
    public ResponseEntity<BaseResponse<ExportResponse>> exportExcel(@RequestBody @Valid OrganizationRpSearchRequest request) {
        return BaseResponse.success(service.exportExcelRp0304(request));
    }
}
