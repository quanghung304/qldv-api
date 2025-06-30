package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.establishment_dissolve.EstablishmentDissolveSearchRequest;
import com.agribank.qldv_api.response.establishment_dissolve.EstablishmentDissolveResponse;
import com.agribank.qldv_api.service.EstablishmentDissolveService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve", produces = "application/json")
@RequiredArgsConstructor
public class EstablishmentDissolveController {
    private final EstablishmentDissolveService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<EstablishmentDissolveResponse>>> search(@RequestBody EstablishmentDissolveSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<EstablishmentDissolveResponse>> get(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.get(id));
    }
}
