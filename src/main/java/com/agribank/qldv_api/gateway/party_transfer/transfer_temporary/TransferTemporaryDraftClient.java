package com.agribank.qldv_api.gateway.party_transfer.transfer_temporary;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.party_transfer.transfer_temporary.TransferTemporaryDraft;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "TransferTemporaryDraftClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-temporary-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferTemporaryDraftClient extends BaseClient<TransferTemporaryDraft, String> {
    @GetMapping("find-by-staffCode/{staffCode}")
    DefaultListResponse<TransferTemporaryDraft> findByStaffCode(@PathVariable String staffCode);
}
