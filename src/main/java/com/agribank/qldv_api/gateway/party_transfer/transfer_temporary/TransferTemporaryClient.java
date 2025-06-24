package com.agribank.qldv_api.gateway.party_transfer.transfer_temporary;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.party_transfer.transfer_temporary.TransferTemporary;
import com.agribank.qldvutils.request.party_transfer.TransferTemporaryFilterRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "TransferTemporaryClient",
        url = "${qldv.database.url}" + "/api/v1/transfer-temporary",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransferTemporaryClient extends BaseClient<TransferTemporary, String> {
    @PostMapping("list")
    BaseResponse<PageResponse<TransferTemporary>> getList(@RequestBody TransferTemporaryFilterRequest request);

    @GetMapping("/{processId}")
    BaseResponse<TransferTemporary> findByProcessId(@PathVariable String processId);

    @GetMapping("find-by-staff-code/{staffCode}")
    BaseResponse<List<TransferTemporary>> findByStaffCode(@PathVariable String staffCode);

}
