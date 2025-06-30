package com.agribank.qldv_api.gateway.party_reinstatement;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.party_reinstatement.PartyReinstatement;
import com.agribank.qldvutils.request.party_reinstatement.PartyReinstatementSearchRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp25Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.party_reinstatement.PartyReinstatementDtoResponse;
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

    @PostMapping("/search-report-25")
    DefaultResponse<PageResponse<PartyReinstatement>> search25(
            @RequestBody SearchRp25Request request
    );
}
