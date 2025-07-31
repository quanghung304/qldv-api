package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.response.form02.DissolveDisbandResponse;
import com.agribank.qldv_api.service.DissolveDisbandService;
import com.agribank.qldvutils.request.form02.dissolve.DissolveDisbandSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve", produces = "application/json")
@RequiredArgsConstructor
public class DissolveDisbandController {
    private final DissolveDisbandService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<DissolveDisbandResponse>>> search(@RequestBody @Valid DissolveDisbandSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DissolveDisbandResponse>> get(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.get(id));
    }
}
