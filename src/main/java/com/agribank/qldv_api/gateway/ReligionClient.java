package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.Religion;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "religionClient", url = "${qldv.database.url}" + "/api/v1/religion", configuration = DatabaseFeignConfiguration.class)
public interface ReligionClient extends BaseClient<Religion, String> {
    @GetMapping("/find-all")
    DefaultListResponse<Religion> findAll();
}
