package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.DvDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "dVDraftClient", url = "${qldv.database.url}" + "/api/v1/dv-draft", configuration = DatabaseFeignConfiguration.class)
public interface DVDraftClient extends BaseClient<DvDraft, String> {
}
