package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.religion.ReligionResponse;
import com.agribank.qldv_api.service.ReligionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/religion")
public class ReligionController {
    private final ReligionService religionService;

    @GetMapping("get-all")
    public ResponseEntity<DefaultListResponse<ReligionResponse>> findALl() {
        return DefaultListResponse.success("Success", religionService.findAll());
    }
}
