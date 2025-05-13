package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.TransformationHistoryDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "TransformationHistoryDraftClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface TransformationHistoryDraftClient {
    @PostMapping("api/v1/history-draft/save")
    BaseResponse<TransformationHistoryDraft> save(
            TransformationHistoryDraft transformationHistoryDraft
    );

    @GetMapping("api/v1/history-draft/get-list")
    DefaultListResponse<TransformationHistoryDraft> getList(
            @RequestParam String newCode,
            @RequestParam Integer status
    );

    @GetMapping("api/v1/history-draft/{id}")
    BaseResponse<TransformationHistoryDraft> findById(
            @PathVariable String id
    );

    @GetMapping("api/v1/history-draft/find-all-by-id")
    List<TransformationHistoryDraft> findAllById(List<String> idList);

    @PutMapping("api/v1/history-draft/save-all")
    List<TransformationHistoryDraft> saveAll(List<TransformationHistoryDraft> updatedDrafts);
}
