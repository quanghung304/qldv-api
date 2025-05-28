package com.agribank.qldv_api.gateway.report26;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.IamFeignConfiguration;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "deceasedDraftClient",
        url = "${qldv.database.url}" + "/api/v1/deceased-draft",
        configuration = IamFeignConfiguration.class)
public interface DeceasedDraftClient extends BaseClient<DeceasedDraft, String> {
}
