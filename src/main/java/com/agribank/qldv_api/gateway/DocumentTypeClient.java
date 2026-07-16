package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.DocumentType;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "documentTypeClient", url = "${qldv.database.url}" + "/api/v1/document-type", configuration = DatabaseFeignConfiguration.class)
public interface DocumentTypeClient extends BaseClient<DocumentType, String> {
}
