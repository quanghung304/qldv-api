package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.ethnic.EthnicResponse;
import com.agribank.qldv_api.service.EthnicService;
import com.agribank.qldvutils.entity.Ethnic;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ethnic")
public class EthnicController {
    private final EthnicService service;

    @GetMapping("/find-all")
    public ResponseEntity<BaseResponse<List<EthnicResponse>>> findAll() {
        return BaseResponse.success(service.getAll());
    }
}
