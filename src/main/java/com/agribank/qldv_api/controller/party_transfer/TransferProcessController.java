package com.agribank.qldv_api.controller.party_transfer;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.party_transfer.TransferProcessService;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.agribank.qldv_api.response.DefaultListResponse.success;

@RestController
@RequestMapping("/api/v1/transfer-process")
@RequiredArgsConstructor
public class TransferProcessController {
    private final TransferProcessService transferProcessService;

    @PostMapping("list")
    public ResponseEntity<DefaultListResponse<TransferProcess>> getList(
            @RequestBody TransferProcessRequest request
            ) {
        return success(transferProcessService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<Object>> getList(@PathVariable String id) {
        return DefaultResponse.success(transferProcessService.getDetail(id));
    }

    @GetMapping("/count/{organizationCode}")
    public ResponseEntity<DefaultResponse<Integer>> countByOrganizationCode(@PathVariable String organizationCode) {
        return DefaultResponse.success(transferProcessService.countByOrganizationCode(organizationCode));
    }
}
