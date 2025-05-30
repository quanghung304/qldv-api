package com.agribank.qldv_api.gateway.partyReinstatement;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.partyReinstatement.PartyReinstatement;
import com.agribank.qldvutils.request.partyReinstatement.PartyReinstatementSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.partyReinstatement.PartyReinstatementDtoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "PartyReinstatement",
        url = "${qldv.database.url}" + "/api/v1/party-reinstatement",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyReinstatementClient extends BaseClient<PartyReinstatement, String> {

    @PostMapping("/search")
    DefaultResponse<PageResponse<PartyReinstatementDtoResponse>> search(
            @RequestBody PartyReinstatementSearchRequest request
    );
}
