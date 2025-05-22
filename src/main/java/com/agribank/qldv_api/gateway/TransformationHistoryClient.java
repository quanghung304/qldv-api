package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.TransformationHistory;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "TransformationHistoryClient",
        url = "${qldv.database.url}" + "/api/v1/history",
        configuration = DatabaseFeignConfiguration.class
)
public interface TransformationHistoryClient extends BaseClient<TransformationHistory, String> {
}
