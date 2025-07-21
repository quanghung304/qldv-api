package com.agribank.qldv_api.gateway.form02.transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrgTransferDetailDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "PartyOrgTransferDetailDraftClient",
        url = "${qldv.database.url}" + "/api/v1/party-org-transfer-detail-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyOrgTransferDetailDraftClient extends BaseClient<PartyOrgTransferDetailDraft, String> {
    @GetMapping("/find-by-ref-id")
    BaseResponse<List<PartyOrgTransferDetailDraft>> findByRefId(@RequestParam(name = "refId") String refId);
}
