package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferTemporaryRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferTemporaryService;
import com.agribank.qldvutils.dto.TransferTemporaryDto;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_temporary.TransferTemporary;
import com.agribank.qldvutils.request.party_transfer.TransferTemporaryFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer-temporary")
@RequiredArgsConstructor
public class TransferTemporaryController {
    private final TransferTemporaryService transferTemporaryService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> createTransferRequest(@RequestBody TransferTemporaryRequest request) {
        return DefaultResponse.success(transferTemporaryService.createRequest(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultResponse<PageResponse<TransferTemporaryDto>>> getList(@RequestBody TransferTemporaryFilterRequest request) {
        return DefaultResponse.success(transferTemporaryService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<TransferTemporary>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(transferTemporaryService.getDetail(id));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> updateTransferForm(@RequestBody TransferTemporaryRequest request) {
        return DefaultResponse.success(transferTemporaryService.update(request));
    }
}
