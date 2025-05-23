package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveRequest;
import com.agribank.qldv_api.request.establishmentDissolveDraft.EDDraftSearchRequest;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.establishmentDissolveDraft.EDDraftResponse;
import com.agribank.qldv_api.service.EstablishmentDissolveDraftService;
import com.agribank.qldvutils.entity.EstablishmentDissolveDraft;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve-draft", produces = "application/json")
@RequiredArgsConstructor
public class EstablishmentDissolveDraftController {
    private final EstablishmentDissolveDraftService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create-or-update")
    public ResponseEntity<BaseResponse<EstablishmentDissolveDraft>> createOrUpdate(@RequestBody @Valid EstablishmentDissolveRequest request) {
        request.validate();
        return BaseResponse.success(service.createOrUpdate(request));
    }

    @PreAuthorize("hasAuthority('QLDV_APPROVER')")
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<List<DraftResponse>>> approve(@RequestBody List<DraftRequest> request) {
        return BaseResponse.success(service.approve(request));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<EDDraftResponse>>> search(@RequestBody EDDraftSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.delete(id), null);
    }
}
