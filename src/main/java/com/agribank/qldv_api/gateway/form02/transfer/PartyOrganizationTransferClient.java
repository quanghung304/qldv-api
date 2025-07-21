package com.agribank.qldv_api.gateway.form02.transfer;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.form02.transfer.PartyOrganizationTransfer;
import com.agribank.qldvutils.request.form02.SearchPartyOrgTransferRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranDraftRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranEntityCreateRequest;
import com.agribank.qldvutils.request.form02.transfer.PartyOrgTranEntityUpdateRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "PartyOrganizationTransferClient",
        url = "${qldv.database.url}" + "/api/v1/party-organization-transfer",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyOrganizationTransferClient extends BaseClient<PartyOrganizationTransfer, String> {
    @PostMapping("/search")
    BaseResponse<PageResponse<PartyOrganizationTransfer>> search(@RequestBody SearchPartyOrgTransferRequest request);

    @PostMapping("/create/entity")
    BaseResponse<Boolean> createEntity(@RequestBody PartyOrgTranEntityCreateRequest request);

    @PostMapping("/update/entity")
    BaseResponse<Boolean> updateEntity(@RequestBody PartyOrgTranEntityUpdateRequest request);

    @PostMapping("/update/entity-draft")
    BaseResponse<Boolean> saveEntityDraft(@RequestBody PartyOrgTranDraftRequest request);
}
