package com.agribank.qldv_api.controller.bcsl;

import com.agribank.qldv_api.service.bcsl_report.tcd.BcslTcdReportService;
import com.agribank.qldv_api.service.dv_report.DVReportService;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10Request;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.report_tcd.SearchRp09Request;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp09Response;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp02Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bcsl-tcd-report")
public class BcslTcdReportController {
    private final BcslTcdReportService service;
    private final DVReportService dvReportService;

    @PostMapping("/search/report-01")
    public ResponseEntity<BaseResponse<PageResponse<BcslTcdRp01Response>>> search(@RequestBody @Valid SearchRp01Request request) {
        return BaseResponse.success(service.searchRp01(request));
    }

    @PostMapping("/search/report-09")
    public ResponseEntity<BaseResponse<PageResponse<BcslTcdRp09Response>>> search(@RequestBody @Valid SearchRp09Request request) {
        return BaseResponse.success(service.searchRp09(request));
    }

    @PostMapping("/search/report-10")
    public ResponseEntity<BaseResponse<PageResponse<BcslDvRp10Response>>> search(@RequestBody @Valid SearchRp10Request request) {
        return BaseResponse.success(dvReportService.searchRp10(request));
    }

    @PostMapping("/search/report-02")
    public ResponseEntity<BaseResponse<PageResponse<BcslTcdRp02Response>>> searchRp02(@RequestBody @Valid SearchRequest request) {
        return BaseResponse.success(service.searchRp02(request));
    }
}

