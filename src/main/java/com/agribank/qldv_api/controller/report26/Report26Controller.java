package com.agribank.qldv_api.controller.report26;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.report26.Report26Service;
import com.agribank.qldvutils.request.report26.Report26SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.report26.Report26DtoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/report_26")
public class Report26Controller {
    private final Report26Service service;

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<Report26DtoResponse>>> search(@RequestBody Report26SearchRequest request) {
        request.validate();
        return DefaultResponse.success(service.search(request));
    }
}
