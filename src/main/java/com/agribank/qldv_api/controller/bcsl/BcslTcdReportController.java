package com.agribank.qldv_api.controller.bcsl;

import com.agribank.qldv_api.service.bcsl_report.tcd.BcslTcdReportService;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
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
    private final BcslTcdReportService bcslTcdReportService;

    @PostMapping("/search/report-01")
    public ResponseEntity<BaseResponse<PageResponse<BcslTcdRp01Response>>> search(@RequestBody @Valid SearchRp01Request request) {
        return BaseResponse.success(bcslTcdReportService.searchRp01(request));
    }
}

