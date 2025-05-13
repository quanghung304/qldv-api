package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.request.establishmentDissolveDraft.EDDraftSearchRequest;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.EstablishmentDissolveDraftResponse.EDDraftResponse;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve-draft", produces = "application/json")
@RequiredArgsConstructor
public class EstablishmentDissolveDraftController {
    private final EstablishmentDissolveDraftService service;

    @PostMapping("/create-or-update")
    public ResponseEntity<BaseResponse<String>> createOrUpdate(@RequestBody EstablishmentDissolveRequest request) {
        request.validate();
        return BaseResponse.success(service.createOrUpdate(request));
    }

//    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<List<DraftResponse>>> approve(@RequestBody List<DraftRequest> request) {
        return BaseResponse.success(service.approve(request));
    }

//    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<EDDraftResponse>>> search(@RequestBody EDDraftSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }
}
