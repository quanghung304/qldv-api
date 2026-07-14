package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Staff;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "staffClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface StaffClient extends BaseClient<Staff, String> {
}
