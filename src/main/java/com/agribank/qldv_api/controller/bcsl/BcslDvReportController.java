package com.agribank.qldv_api.controller.bcsl;

import com.agribank.qldv_api.service.bcsl_report.dv.BcslDvReportService;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.DvRp18Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bcsl-dv-report")
public class BcslDvReportController {
    private final BcslDvReportService service;

    @PostMapping("/search/report-18")
    public ResponseEntity<BaseResponse<PageResponse<DvRp18Response>>> search(@RequestBody @Valid SearchRequest request) {
        return BaseResponse.success(service.searchRp18(request));
    }
}
