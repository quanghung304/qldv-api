package com.agribank.qldv_api.gateway.report26;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.IamFeignConfiguration;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "partyActivityExemptionDraftClient",
        url = "${qldv.database.url}" + "/api/v1/party-activity-exemption-draft",
        configuration = IamFeignConfiguration.class)
public interface PartyActivityExemptionDraftClient extends BaseClient<PartyActivityExemptionDraft, String> {
}
