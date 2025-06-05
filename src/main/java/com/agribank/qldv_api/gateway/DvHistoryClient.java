package com.agribank.qldv_api.gateway;

import com.agribank.qldvutils.entity.DvHistory;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "HistoryClient",
        url = "${qldv.database.url}" + "/api/v1/dv-history",
        configuration = DatabaseFeignConfiguration.class
)
public interface DvHistoryClient extends BaseClient<DvHistory, String>{
}
