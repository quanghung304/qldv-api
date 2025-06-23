package com.agribank.qldv_api.gateway.party_transfer.transfer_within_base;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBase;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "TransferWithinBaseClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-base",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferWithinBaseClient extends BaseClient<TransferWithinBase, String> {
    @PostMapping("list")
    BaseResponse<PageResponse<TransferWithinBase>> getList(@RequestBody TransferToFilterRequest request);
}
