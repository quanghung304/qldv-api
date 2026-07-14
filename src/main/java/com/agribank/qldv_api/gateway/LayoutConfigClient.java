package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.LayoutConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "layout", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface LayoutConfigClient extends BaseClient<LayoutConfig, String> {
    @PostMapping("api/v1/layout/search-layout")
    DefaultResponse<LayoutConfig> searchByDescription(
            @RequestBody String description
    );
}
