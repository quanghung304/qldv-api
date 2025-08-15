package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.party_reinstatement.PartyReinstatementRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.party_reinstatement.PartyReinstatementResponse;
import com.agribank.qldv_api.service.party_reinstatement.PartyReinstatementService;
import com.agribank.qldvutils.request.party_reinstatement.PartyReinstatementSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.party_reinstatement.PartyReinstatementDtoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/party-reinstatement", produces = "application/json")
@RequiredArgsConstructor
public class PartyReinstatementController {

    private final PartyReinstatementService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody PartyReinstatementRequest request) {
        request.validate();
        return BaseResponse.success(service.create(request), null);
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createDelete(id), null);
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResponse<PartyReinstatementResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<PartyReinstatementDtoResponse>>> search(@RequestBody PartyReinstatementSearchRequest request) {
        request.validate();
        return DefaultResponse.success(service.search(request));
    }

    @GetMapping("/draft/detail")
    public ResponseEntity<BaseResponse<PartyReinstatementResponse>> getDraftDetail(@RequestParam(name = "id") String id) {
        return BaseResponse.success(service.getDraftDetail(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft/update")
    public ResponseEntity<BaseResponse<String>> update(@RequestBody PartyReinstatementRequest request) {
        request.validate();
        return BaseResponse.success(service.updateDraft(request), null);
    }
}
