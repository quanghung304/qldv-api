package com.agribank.qldv_api.gateway.form02.transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrganizationTransferDraft;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "PartyOrganizationTransferDraftClient",
        url = "${qldv.database.url}" + "/api/v1/party-organization-transfer-draft",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyOrganizationTransferDraftClient extends BaseClient<PartyOrganizationTransferDraft, String> {
}
