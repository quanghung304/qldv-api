package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.establishmentDissolveDraft.EDDraftSearchRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.EstablishmentDissolveDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "establishmentDissolveDraft", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface EstablishmentDissolveDraftClient {

    @PostMapping("api/v1/establishment-dissolve-draft/save")
    DefaultResponse<EstablishmentDissolveDraft> save(
            @RequestBody EstablishmentDissolveDraft request
    );

    @PostMapping("api/v1/establishment-dissolve-draft/save/all")
    DefaultResponse<List<EstablishmentDissolveDraft>> saveAll(
            @RequestBody List<EstablishmentDissolveDraft> request
    );

    @GetMapping("api/v1/establishment-dissolve-draft/find-by-id/{id}")
    DefaultResponse<EstablishmentDissolveDraft> findById(
            @PathVariable(name = "id") String id
    );

    @PostMapping("api/v1/establishment-dissolve-draft/search")
    DefaultResponse<PageResponse<EstablishmentDissolveDraft>> search(
            @RequestBody EDDraftSearchRequest request
    );

    @PostMapping("api/v1/establishment-dissolve-draft/find-all-by-id")
    DefaultResponse<List<EstablishmentDissolveDraft>> findAllByIds(
            @RequestBody List<String> ids
    );

    @DeleteMapping("api/v1/establishment-dissolve-draft/delete-by-id/{id}")
    DefaultResponse<EstablishmentDissolveDraft> delete(
            @PathVariable(name = "id") String id
    );

    @GetMapping("api/v1/establishment-dissolve-draft/find-pending/{code}")
    DefaultListResponse<EstablishmentDissolveDraft> findPendingDraftByCode(
            @PathVariable String code
    );
}
