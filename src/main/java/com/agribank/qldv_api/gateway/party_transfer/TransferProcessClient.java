package com.agribank.qldv_api.gateway.party_transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "TransferProcessClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-process",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferProcessClient extends BaseClient<TransferProcess, String> {
    @PostMapping("list")
    BaseResponse<PageResponse<TransferProcess>> getList(@RequestBody TransferProcessRequest request);

    @GetMapping("count/{organizationCode}")
    BaseResponse<Integer> countByOrganizationCode(@PathVariable String organizationCode);

    @GetMapping("find-processing")
    DefaultListResponse<TransferProcess> findProcessingTransfer(
            @RequestParam String staffCode,
            @RequestParam Integer type
    );
}
