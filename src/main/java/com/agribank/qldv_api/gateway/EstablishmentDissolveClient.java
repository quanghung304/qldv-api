package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.establishment_dissolve.EstablishmentDissolveSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolve;
import com.agribank.qldvutils.request.form02.ApproveDissolveRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "establishmentDissolve", url = "${qldv.database.url}" + "/api/v1/establishment-dissolve", configuration = DatabaseFeignConfiguration.class)
public interface EstablishmentDissolveClient extends BaseClient<EstablishmentDissolve, String> {
    @PostMapping("api/v1/establishment-dissolve/search")
    DefaultResponse<PageResponse<EstablishmentDissolve>> search(
            @RequestBody EstablishmentDissolveSearchRequest request
    );

    @PutMapping("/save-entities")
    BaseResponse<Boolean> saveEntities(@RequestBody @Valid ApproveDissolveRequest request);
}
