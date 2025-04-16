package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.request.apiLog.SearchApiLogRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.ApiLogResponse;
import com.agribank.qldv_api.service.ApiLogService;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/api-log")
@RequiredArgsConstructor
public class ApiLogController {
    private final ApiLogService apiLogService;

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<ApiLogResponse>>> search(@RequestBody SearchApiLogRequest request) {
        request.validate();
        return DefaultResponse.success(apiLogService.search(request));
    }
}
