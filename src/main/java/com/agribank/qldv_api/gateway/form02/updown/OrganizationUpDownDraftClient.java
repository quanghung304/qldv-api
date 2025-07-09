package com.agribank.qldv_api.gateway.form02.updown;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDownDraft;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "OrganizationUpDownDraftClient",
        url = "${qldv.database.url}" + "/api/v1/updown-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface OrganizationUpDownDraftClient extends BaseClient<OrganizationUpDownDraft, String> {
    @PostMapping("save")
    BaseResponse<OrganizationUpDownDraft> save(
            OrganizationUpDownDraft OrganizationUpDownDraft
    );

    @GetMapping("get-list")
    DefaultListResponse<OrganizationUpDownDraft> getList(
            @RequestParam String newCode,
            @RequestParam Integer status
    );

    @GetMapping("/find-pending/{code}")
    DefaultListResponse<OrganizationUpDownDraft> findPendingDraftByCode(
            @PathVariable String code
    );
}
