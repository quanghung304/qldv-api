package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.response.establishmentDissolve.EstablishmentDissolveResponse;
import com.agribank.qldv_api.service.EstablishmentDissolveService;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve", produces = "application/json")
@RequiredArgsConstructor
public class EstablishmentDissolveController {
    private final EstablishmentDissolveService service;

    @PostMapping("")
    public ResponseEntity<BaseResponse<EstablishmentDissolveResponse>> create(@RequestBody EstablishmentDissolveRequest request) {
        request.validate();
        return BaseResponse.success(service.createOrUpdate(request));
    }
}
