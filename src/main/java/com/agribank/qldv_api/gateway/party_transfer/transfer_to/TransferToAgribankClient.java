package com.agribank.qldv_api.gateway.party_transfer.transfer_to;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_to.TransferToAgribank;
import com.agribank.qldvutils.request.party_transfer.ApproveTransferToRequest;
import com.agribank.qldvutils.request.party_transfer.TransferToFilterRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp28Request;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.dv_report.DvRp28Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "TransferToAgribankClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-to-agri",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferToAgribankClient extends BaseClient<TransferToAgribank, String> {
    @PostMapping("list")
    BaseResponse<PageResponse<TransferToAgribank>> getList(@RequestBody TransferToFilterRequest request);

    @GetMapping("/{processId}")
    BaseResponse<TransferToAgribank> findByProcessId(@PathVariable String processId);

    @GetMapping("find-by-staff-code/{staffCode}")
    BaseResponse<TransferToAgribank> findByStaffCode(@PathVariable String staffCode);

    @PostMapping("/search-rp-28")
    BaseResponse<PageResponse<DvRp28Response>> searchRp28(@RequestBody SearchRp28Request request);

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody ApproveTransferToRequest request);
}
