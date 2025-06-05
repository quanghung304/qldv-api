package com.agribank.qldv_api.gateway.party_reinstatement;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;

import com.agribank.qldvutils.entity.party_reinstatement.PartyReinstatementDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "PartyReinstatementDraft",
        url = "${qldv.database.url}" + "/api/v1/party-reinstatement-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyReinstatementDraftClient extends BaseClient<PartyReinstatementDraft, String> {
}
