package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferToAgribankRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferToAgribankService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.request.PagingRequest;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.DefaultListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer-to-agribank")
@RequiredArgsConstructor
public class TransferToAgribankController {
    private final TransferToAgribankService transferToAgribankService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> createTransferRequest(@RequestBody TransferToAgribankRequest transferToAgribankRequest) {
        return DefaultResponse.success(transferToAgribankService.createRequest(transferToAgribankRequest));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultListResponse<TransferToAgribank>> getList(@RequestBody TransferToFilterRequest request) {
        return DefaultListResponse.success(transferToAgribankService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<TransferToAgribank>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(transferToAgribankService.getDetail(id));
    }
}
