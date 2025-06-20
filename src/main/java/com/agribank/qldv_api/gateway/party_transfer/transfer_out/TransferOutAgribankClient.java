package com.agribank.qldv_api.gateway.party_transfer.transfer_out;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribank;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "TransferOutAgribankClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-out",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferOutAgribankClient extends BaseClient<TransferOutAgribank, String> {
    @PostMapping("list")
    BaseResponse<PageResponse<TransferOutAgribank>> getList(@RequestBody TransferToFilterRequest request);

    @GetMapping("find-by-staffcode/{staffCode}")
    BaseResponse<TransferOutAgribank> findByStaffCode(@PathVariable String staffCode);
}
