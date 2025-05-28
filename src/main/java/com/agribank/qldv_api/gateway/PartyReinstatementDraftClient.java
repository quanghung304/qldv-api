package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.PartyReinstatementDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "PartyReinstatementDraft",
        url = "${qldv.database.url}" + "/api/v1/party-reinstatement-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyReinstatementDraftClient extends BaseClient<PartyReinstatementDraft, String>{
}
