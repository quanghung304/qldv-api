package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.request.party_transfer.TransferOutAgribankRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferOutAgribankService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer-out")
@RequiredArgsConstructor
public class TransferOutAgribankController {
    private final TransferOutAgribankService service;

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

    @PutMapping("update")
    public ResponseEntity<DefaultResponse<Request>> update(@RequestBody TransferOutAgribankRequest request) {
        return DefaultResponse.success(service.update(request));
    }
}
