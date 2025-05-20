package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldvutils.entity.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/request")
public class RequestController {
    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<DefaultListResponse<Request>> getList() {
        return DefaultListResponse.success(requestService.getList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<Request>> getById(@PathVariable String id) {
        return DefaultResponse.success(requestService.getById(id));
    }

}
