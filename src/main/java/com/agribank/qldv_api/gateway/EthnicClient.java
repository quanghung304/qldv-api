package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.Ethnic;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "ethnicClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface EthnicClient {
    @GetMapping("api/v1/ethnic/find-all")
    DefaultResponse<List<Ethnic>> findAll();
}
