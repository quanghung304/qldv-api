package com.agribank.qldv_api.gateway.report26;

import com.agribank.qldv_api.gateway.BaseClient;
import com.agribank.qldv_api.gateway.IamFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.report26.PartyActivityExemption;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "partyActivityExemptionClient", url = "${qldv.database.url}" + "/api/v1/party-activity-exemption", configuration = IamFeignConfiguration.class)
public interface PartyActivityExemptionClient extends BaseClient<PartyActivityExemption, String> {
    @GetMapping("/detail/{id}")
    DefaultResponse<RP26DetailResponse> getDetail(@PathVariable("id") String id);
}
