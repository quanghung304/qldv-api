package com.agribank.qldv_api.gateway.form02.dissolve;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.form02.dissolve.DissolveDisband;
import com.agribank.qldvutils.request.form02.dissolve.ApproveDissolveRequest;
import com.agribank.qldvutils.request.form02.dissolve.DissolveDisbandSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "DissolveDisbandClient", url = "${qldv.database.url}" + "/api/v1/dissolve-disband", configuration = DatabaseFeignConfiguration.class)
public interface DissolveDisbandClient extends BaseClient<DissolveDisband, String> {
    @PostMapping("/search")
    DefaultResponse<PageResponse<DissolveDisband>> search(
            @RequestBody DissolveDisbandSearchRequest request
    );

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody @Valid ApproveDissolveRequest request);
}
