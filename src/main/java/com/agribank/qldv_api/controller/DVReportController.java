package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.dv_report.*;
import com.agribank.qldv_api.service.dv_report.DVReportService;
import com.agribank.qldvutils.request.report_dv.*;
import com.agribank.qldvutils.request.report_dv.SearchRp23Request;
import com.agribank.qldvutils.request.report_dv.SearchRp21Request;
import com.agribank.qldvutils.request.report_dv.SearchRp24Request;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.request.report_dv.SearchRp34Request;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import com.agribank.qldvutils.response.dv_report.DvRp30Response;
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

    @PostMapping("/search/report-24")
    public ResponseEntity<BaseResponse<PageResponse<DvRp24Response>>> search(@RequestBody @Valid SearchRp24Request request) {
        return BaseResponse.success(service.search24(request));
    }

    @PostMapping("/search/report-25")
    public ResponseEntity<BaseResponse<PageResponse<DvRp25Response>>> search(@RequestBody @Valid SearchRp25Request request) {
        return BaseResponse.success(service.search25(request));
    }

    @PostMapping("/search/report-29")
    public ResponseEntity<BaseResponse<PageResponse<DvRp29Response>>> search(@RequestBody @Valid SearchReport29Request request) {
        return BaseResponse.success(service.search29(request));
    }

    @PostMapping("/search/report-34")
    public ResponseEntity<BaseResponse<PageResponse<DvRp34Response>>> search(@RequestBody @Valid SearchRp34Request request) {
        return BaseResponse.success(service.search34(request));
    }

    @PostMapping("/search/report-21")
    public ResponseEntity<BaseResponse<PageResponse<DvRp21Response>>> search(@RequestBody @Valid SearchRp21Request request) {
        return BaseResponse.success(service.searchRp21(request));
    }

    @PostMapping("/search/report-23")
    public ResponseEntity<BaseResponse<PageResponse<DvRp23Response>>> search(@RequestBody @Valid SearchRp23Request request) {
        return BaseResponse.success(service.searchRp23(request));
    }

    @PostMapping("/search/report-28")
    public ResponseEntity<BaseResponse<PageResponse<DvRp28Response>>> search(@RequestBody @Valid SearchRp28Request request) {
        return BaseResponse.success(service.searchRp28(request));
    }

    @PostMapping("/search/report-30")
    public ResponseEntity<BaseResponse<PageResponse<DvRp30Response>>> search(@RequestBody @Valid SearchRp30Request request) {
        return BaseResponse.success(service.searchRp30(request));
    }
}
