package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferWithinAgribankRequest;
import com.agribank.qldv_api.request.party_transfer.TransferWithinAgribankUpdateRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.party_transfer.TransferWithinAgribankResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.party_transfer.TransferWithinAgribankService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.request.party_transfer.TransferWithinAgribankSearch;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfer-within-agribank")
public class TransferWithinAgribankController {
    private final TransferWithinAgribankService service;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> createTransferRequest(@RequestBody TransferWithinAgribankRequest request) {
        return DefaultResponse.success(service.createRequest(request));
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<TransferWithinAgribankResponse>>> search(@RequestBody TransferWithinAgribankSearch request) {
        return DefaultResponse.success(service.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<TransferWithinAgribankResponse>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(service.getDetail(id));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> updateTransferForm(@RequestBody TransferWithinAgribankUpdateRequest request) {
        return DefaultResponse.success(service.update(request));
    }

    @GetMapping("/draft/detail")
    public ResponseEntity<DefaultResponse<TransferWithinAgribankResponse>> getDraftDetail(@RequestParam(name = "id") String id) {
        return DefaultResponse.success(service.getDraftDetail(id));
    }

    @PutMapping("/draft/update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<RequestResponse>> updateDraft(@RequestBody TransferWithinAgribankUpdateRequest request) {
        return DefaultResponse.success(service.updateDraft(request));
    }
}
