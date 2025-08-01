package com.agribank.qldv_api.gateway.form02.dissolve;

import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.form02.dissolve.DissolveDisbandDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "DissolveDisbandDraftClient", url = "${qldv.database.url}"+ "/api/v1/dissolve-disband-draft", configuration = DatabaseFeignConfiguration.class)
public interface DissolveDisbandDraftClient {

    @PostMapping("/save")
    DefaultResponse<DissolveDisbandDraft> save(
            @RequestBody DissolveDisbandDraft request
    );

    @PostMapping("/save/all")
    DefaultResponse<List<DissolveDisbandDraft>> saveAll(
            @RequestBody List<DissolveDisbandDraft> request
    );

    @GetMapping("/find-by-id/{id}")
    DefaultResponse<DissolveDisbandDraft> findById(
            @PathVariable(name = "id") String id
    );

    @PostMapping("/find-all-by-id")
    DefaultResponse<List<DissolveDisbandDraft>> findAllByIds(
            @RequestBody List<String> ids
    );

    @DeleteMapping("/delete-by-id/{id}")
    DefaultResponse<DissolveDisbandDraft> delete(
            @PathVariable(name = "id") String id
    );

    @GetMapping("/find-pending/{code}")
    DefaultListResponse<DissolveDisbandDraft> findPendingDraftByCode(
            @PathVariable String code
    );
}
