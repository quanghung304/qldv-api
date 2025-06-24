package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.enums.EExcelColumnInfo;
import com.agribank.qldv_api.response.dv_report.DvRp24Response;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.service.dv_report.DVReportService;
import com.agribank.qldv_api.service.dv_report.ExportDVRp24Service;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
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
@RequestMapping(value = "/api/v1/dv-report", produces = "application/json")
public class DVReportController {
    private final DVReportService service;
    private final ExportDVRp24Service exportDVRp24Service;

    @PostMapping("/search/report-24")
    public ResponseEntity<BaseResponse<PageResponse<DvRp24Response>>> search(@RequestBody @Valid SearchRp24Request request) {
        return BaseResponse.success(service.search24(request));
    }

    @PostMapping("/export/report-24")
    public ResponseEntity<BaseResponse<ExportResponse>> export24(@RequestBody @Valid SearchRp24Request request) {
        return BaseResponse.success(exportDVRp24Service.exportData(request, EExcelColumnInfo.BC_24_DSDV.getName(), "", EExcelColumnInfo.BC_24_DSDV.name(), 1, EExcelColumnInfo.BC_24_DSDV.getName()));
    }
}
