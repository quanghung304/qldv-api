package com.agribank.qldv_api.gateway.party_transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "TransferProcessClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-process",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferProcessClient extends BaseClient<TransferProcess, String> {
}
