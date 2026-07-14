package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.AuditLog;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "auditLogClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface AuditLogClient extends BaseClient<AuditLog, String> {
}
