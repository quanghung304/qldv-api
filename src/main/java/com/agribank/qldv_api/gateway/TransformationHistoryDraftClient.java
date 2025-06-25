package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.form02.updown.TransformationHistoryDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "TransformationHistoryDraftClient",
        url = "${qldv.database.url}" + "/api/v1/history-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransformationHistoryDraftClient extends BaseClient<TransformationHistoryDraft, String> {
    @PostMapping("save")
    BaseResponse<TransformationHistoryDraft> save(
            TransformationHistoryDraft transformationHistoryDraft
    );

    @GetMapping("get-list")
    DefaultListResponse<TransformationHistoryDraft> getList(
            @RequestParam String newCode,
            @RequestParam Integer status
    );

    @GetMapping("/find-pending/{code}")
    DefaultListResponse<TransformationHistoryDraft> findPendingDraftByCode(
            @PathVariable String code
    );
}
