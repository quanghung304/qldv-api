package com.agribank.qldv_api.gateway.party_transfer.transfer_within_base;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_base.TransferWithinBaseDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "TransferWithinBaseDraftClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-base-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferWithinBaseDraftClient extends BaseClient<TransferWithinBaseDraft, String> {
    @GetMapping("find-by-staff-code/{staffCode}")
    BaseResponse<TransferWithinBaseDraft> findByStaffCode(@PathVariable String staffCode);
}
