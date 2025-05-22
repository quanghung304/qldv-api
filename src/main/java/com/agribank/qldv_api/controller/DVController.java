package com.agribank.qldv_api.controller;


import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.dv.DVResponse;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dv")
public class DVController {
    private final DVService service;

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<DVResponse>>> search(@RequestBody SearchDVRequest request) {
        return DefaultResponse.success(service.search(request));
    }
}
