package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Document;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "documentClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface DocumentClient extends BaseClient<Document, String> {
}
