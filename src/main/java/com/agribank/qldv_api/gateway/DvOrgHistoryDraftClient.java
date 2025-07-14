package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.DvHistory;
import com.agribank.qldvutils.entity.DvOrgHistoryDraft;
import com.agribank.qldvutils.entity.form02.split.OrganizationSplitDetailDraft;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "dvOrgHistoryDraftClient",
        url = "${qldv.database.url}" + "/api/v1/dv-org-history-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface DvOrgHistoryDraftClient extends BaseClient<DvOrgHistoryDraft, String>{
    @GetMapping("find-by-ref-id/{refId}")
    DefaultListResponse<DvOrgHistoryDraft> findByRefId(@PathVariable String refId);
}

