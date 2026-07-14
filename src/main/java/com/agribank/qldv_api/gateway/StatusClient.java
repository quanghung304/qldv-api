package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Status;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "statusClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface StatusClient extends BaseClient<Status, String> {
}
