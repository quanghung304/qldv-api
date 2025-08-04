package com.agribank.qldv_api.controller.report;

import com.agribank.qldv_api.response.tcd.Rp0304Response;
import com.agribank.qldv_api.response.tcd.Rp17Response;
import com.agribank.qldv_api.service.tcd_report.TcdReportService;
import com.agribank.qldvutils.dto.Report05BcdsDto;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.request.report_tcd.SearchRp17Request;
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

    @PostMapping("/search/report-03-04")
    public ResponseEntity<BaseResponse<PageResponse<Rp0304Response>>> search(@RequestBody @Valid OrganizationRpSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search0304(request));
    }

    @PostMapping("/search/report-05")
    public ResponseEntity<BaseResponse<PageResponse<Report05BcdsDto>>> searchRp05(@RequestBody @Valid SearchRpRequest request) {
        request.validate();
        return BaseResponse.success(service.searchRp05(request));
    }

    @PostMapping("/search/report-17")
    public ResponseEntity<BaseResponse<PageResponse<Rp17Response>>> search(@RequestBody @Valid SearchRp17Request request) {
        request.validate();
        return BaseResponse.success(service.searchRp17(request));
    }
}
