package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferWithinBaseRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferWithinBaseService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBase;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer-within-base")
@RequiredArgsConstructor
public class TransferWithinBaseController {
    private final TransferWithinBaseService service;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> create(@RequestBody TransferWithinBaseRequest request) {
        return DefaultResponse.success(service.createOrUpdate(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultResponse<PageResponse<TransferWithinBase>>> getList(@RequestBody TransferToFilterRequest request) {
        return DefaultResponse.success(service.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<TransferWithinBase>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(service.getDetail(id));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> update(@RequestBody TransferWithinBaseRequest request) {
        return DefaultResponse.success(service.createOrUpdate(request));
    }
}
