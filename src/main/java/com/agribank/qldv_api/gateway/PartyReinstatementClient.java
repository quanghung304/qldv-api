package com.agribank.qldv_api.gateway;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "PartyReinstatement",
        url = "${qldv.database.url}" + "/api/v1/party-reinstatement",
        configuration = DatabaseFeignConfiguration.class
)
public interface PartyReinstatementClient {
}
