package com.agribank.qldv_api.gateway.party_transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "TransferProcessClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-process",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferProcessClient extends BaseClient<TransferProcess, String> {
    @PostMapping("list")
    DefaultListResponse<TransferProcess> getList(@RequestBody TransferProcessRequest request);

    @GetMapping("count/{organizationCode}")
    BaseResponse<Integer> countByOrganizationCode(@PathVariable String organizationCode);
}
