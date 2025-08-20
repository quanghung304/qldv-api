package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferOutAgribankRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferOutAgribankService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribankDraft;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer-out")
@RequiredArgsConstructor
public class TransferOutAgribankController {
    private final TransferOutAgribankService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("create")
    public ResponseEntity<DefaultResponse<Request>> createRequest(@RequestBody TransferOutAgribankRequest request) {
        return DefaultResponse.success(service.create(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultResponse<PageResponse<TransferOutAgribank>>> getList(@RequestBody TransferToFilterRequest request) {
        return DefaultResponse.success(service.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<TransferOutAgribank>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(service.getDetail(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("update")
    public ResponseEntity<DefaultResponse<Request>> update(@RequestBody TransferOutAgribankRequest request) {
        return DefaultResponse.success(service.update(request));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<DefaultResponse<TransferOutAgribankDraft>> getDraft(@PathVariable String id) {
        return DefaultResponse.success(service.getDraft(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft")
    public ResponseEntity<DefaultResponse<String>> updateDraft(@RequestBody TransferOutAgribankRequest request) {
        return DefaultResponse.success(service.updateDraft(request));
    }
}
