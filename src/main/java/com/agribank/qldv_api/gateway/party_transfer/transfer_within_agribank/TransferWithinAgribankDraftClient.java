package com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribankDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "TransferWithinAgribankDraftClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-within-agribank-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferWithinAgribankDraftClient extends BaseClient<TransferWithinAgribankDraft, String> {
}
