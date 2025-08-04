package com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribank;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRpRequest;
import com.agribank.qldvutils.request.party_transfer.ApproveTransferWithinRequest;
import com.agribank.qldvutils.request.party_transfer.TransferWithinAgribankSearch;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp30Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/find-by-process/{processId}")
    DefaultResponse<TransferWithinAgribank> findByProcessId(
            @PathVariable(name = "processId") String processId
    );

    @PostMapping("/search-rp-30")
    DefaultResponse<PageResponse<DvRp30Response>> searchRp30(
            @RequestBody SearchRpRequest request
    );

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody ApproveTransferWithinRequest request);
}
