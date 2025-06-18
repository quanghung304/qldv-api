package com.agribank.qldv_api.gateway.party_transfer.transfer_to;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribankDraft;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "TransferToAgribankDraftClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-to-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferToAgribankDraftClient extends BaseClient<TransferToAgribankDraft, String> {
    @GetMapping("find-by-staffCode/{staffCode}")
    DefaultListResponse<TransferToAgribankDraft> findByStaffCode(@PathVariable String staffCode);
}
