package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.DocumentTemplate;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "documentTemplateClient", url = "${qldv.database.url}" + "/api/v1/document-template", configuration = DatabaseFeignConfiguration.class)
public interface DocumentTemplateClient extends BaseClient<DocumentTemplate, String> {
}
