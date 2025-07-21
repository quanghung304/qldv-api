package com.agribank.qldv_api.gateway.form02.transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrgTransferDetail;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "PartyOrgTransferDetailClient",
        url = "${qldv.database.url}" + "/api/v1/party-org-transfer-detail",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyOrgTransferDetailClient extends BaseClient<PartyOrgTransferDetail, String> {
    @GetMapping("/find-by-ref-id")
    BaseResponse<List<PartyOrgTransferDetail>> findByRefId(@RequestParam(name = "refId") String refId);
}
