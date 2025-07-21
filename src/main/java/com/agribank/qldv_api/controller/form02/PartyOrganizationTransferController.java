package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.PartyOrgTransferRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.form02.transfer.PartyOrgTranResponse;
import com.agribank.qldv_api.response.form02.transfer.PartyOrganizationTransferResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.form02.PartyOrganizationTransferService;
import com.agribank.qldvutils.request.form02.SearchPartyOrgTransferRequest;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/party-organization-transfer")
@RequiredArgsConstructor
public class PartyOrganizationTransferController {
    private final PartyOrganizationTransferService service;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<RequestResponse>> create(@RequestBody @Valid PartyOrgTransferRequest request) {
        request.validate();
        return DefaultResponse.success(service.create(request));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<RequestResponse>> update(@RequestBody @Valid PartyOrgTransferRequest request) {
        return DefaultResponse.success(service.update(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<PartyOrgTranResponse>> get(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(service.get(id));
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<PartyOrganizationTransferResponse>>> search(@RequestBody @Valid SearchPartyOrgTransferRequest request) {
        return DefaultResponse.success(service.search(request));
    }
}
