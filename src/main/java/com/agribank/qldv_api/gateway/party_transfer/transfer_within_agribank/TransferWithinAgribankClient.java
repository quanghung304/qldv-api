package com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribank;
import com.agribank.qldvutils.request.party_transfer.TransferWithinAgribankSearch;
import com.agribank.qldvutils.request.report26.Report26SearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.report26.Report26DtoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "TransferWithinAgribankClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-within-agribank",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferWithinAgribankClient extends BaseClient<TransferWithinAgribank, String> {
    @PostMapping("/search")
    DefaultResponse<PageResponse<TransferWithinAgribank>> search(
            @RequestBody TransferWithinAgribankSearch request
    );
}
