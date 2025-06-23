package com.agribank.qldv_api.gateway.party_transfer.transfer_out;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_out.TransferOutAgribankDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "TransferOutAgribankDraftClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-out-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferOutAgribankDraftClient extends BaseClient<TransferOutAgribankDraft, String> {
    @GetMapping("/{staffCode}")
    DefaultListResponse<TransferOutAgribankDraft> findByStaffCode(@PathVariable String staffCode);
}
