package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.establishmentDissolve.EstablishmentDissolveSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolve;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "establishmentDissolve", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface EstablishmentDissolveClient {
    @PostMapping("api/v1/establishment-dissolve/save")
    DefaultResponse<EstablishmentDissolve> save(
            @RequestBody EstablishmentDissolve request
    );

    @PostMapping("api/v1/establishment-dissolve/save/all")
    DefaultResponse<List<EstablishmentDissolve>> saveAll(
            @RequestBody List<EstablishmentDissolve> request
    );

    @GetMapping("api/v1/establishment-dissolve/find-by-id/{id}")
    DefaultResponse<EstablishmentDissolve> findById(
            @PathVariable(name = "id") String id
    );

    @PostMapping("api/v1/establishment-dissolve/search")
    DefaultResponse<PageResponse<EstablishmentDissolve>> search(
            @RequestBody EstablishmentDissolveSearchRequest request
    );
}
