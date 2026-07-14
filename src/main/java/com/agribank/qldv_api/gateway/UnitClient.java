package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Unit;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "unitClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface UnitClient extends BaseClient<Unit, String> {
}
