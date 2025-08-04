package com.agribank.qldv_api.controller.report;

import com.agribank.qldv_api.request.dv_report.SearchReport29Request;
import com.agribank.qldv_api.response.dv_report.*;
import com.agribank.qldv_api.service.dv_report.DVReportService;
import com.agribank.qldvutils.dto.Report31Dto;
import com.agribank.qldvutils.dto.SearchRp33Dto;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.report_dv.*;
import com.agribank.qldvutils.request.report_dv.SearchRp07Request;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.Report32Response;
import com.agribank.qldvutils.response.dv_report.DvRp22Response;
import com.agribank.qldvutils.response.dv_report.DvRp23Response;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import com.agribank.qldvutils.response.report07.Report07DtoResponse;
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
    public ResponseEntity<BaseResponse<PageResponse<DvRp24Response>>> searchRp24(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.search24(request));
    }

    @PostMapping("/search/report-25")
    public ResponseEntity<BaseResponse<PageResponse<DvRp25Response>>> searchRp25(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.search25(request));
    }

    @PostMapping("/search/report-29")
    public ResponseEntity<BaseResponse<PageResponse<DvRp29Response>>> search(@RequestBody @Valid SearchReport29Request request) {
        return BaseResponse.success(service.search29(request));
    }

    @PostMapping("/search/report-34")
    public ResponseEntity<BaseResponse<PageResponse<DvRp34Response>>> searchRp34(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.search34(request));
    }

    @PostMapping("/search/report-21")
    public ResponseEntity<BaseResponse<PageResponse<DvRp21Response>>> searchRp21(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp21(request));
    }

    @PostMapping("/search/report-23")
    public ResponseEntity<BaseResponse<PageResponse<DvRp23Response>>> searchRp23(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp23(request));
    }

    @PostMapping("/search/report-28")
    public ResponseEntity<BaseResponse<PageResponse<DvRp28Response>>> searchRp28(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp28(request));
    }

    @PostMapping("/search/report-30")
    public ResponseEntity<BaseResponse<PageResponse<DvRp30Response>>> searchRp30(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp30(request));
    }

    @PostMapping("/search/report-22")
    public ResponseEntity<BaseResponse<PageResponse<DvRp22Response>>> searchRp22(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp22(request));
    }

    @PostMapping("/search/report-07")
    public ResponseEntity<BaseResponse<PageResponse<Report07DtoResponse>>> search(@RequestBody @Valid SearchRp07Request request) {
        return BaseResponse.success(service.search07(request));
    }

    @PostMapping("/search/report-31")
    public ResponseEntity<BaseResponse<PageResponse<Report31Dto>>> searchRp31(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.search31(request));
    }


    @PostMapping("/search/report-33")
    public ResponseEntity<BaseResponse<PageResponse<SearchRp33Dto>>> searchRp33(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.search33(request));
    }

    @PostMapping("/search/report-32")
    public ResponseEntity<BaseResponse<PageResponse<Report32Response>>> search(@RequestBody @Valid SearchRp32Request request) {
        return BaseResponse.success(service.searchRp32(request));
    }

    @PostMapping("/search/report-10")
    public ResponseEntity<BaseResponse<PageResponse<BcslDvRp10Response>>> search(@RequestBody @Valid SearchRpRequest request) {
        return BaseResponse.success(service.searchRp10(request));
    }
}
