package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.request.FormResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldvutils.request.FilterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/request")
public class RequestController {
    private final RequestService requestService;

    @GetMapping("form-code")
    public ResponseEntity<DefaultListResponse<FormResponse>> getFormCodes() {
        return DefaultListResponse.success(requestService.getFormList());
    }

    @PostMapping("list")
    public ResponseEntity<DefaultListResponse<RequestResponse>> getList(
            @RequestBody @Valid FilterRequest filterRequest
    ) {
        return DefaultListResponse.success(requestService.getList(filterRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<Object>> getById(@PathVariable String id) {
        return DefaultResponse.success(requestService.getById(id));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('QLDV_APPROVER')")
    public ResponseEntity<DefaultResponse<String>> approveRequest(@RequestBody List<ApproveRequest> request) {
        return DefaultResponse.success(requestService.approveRequest(request));
    }
}
