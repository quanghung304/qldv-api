package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.DocumentType;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "documentTypeClient", url = "${qldv.database.url}" + "/api/v1/document-type", configuration = DatabaseFeignConfiguration.class)
public interface DocumentTypeClient extends BaseClient<DocumentType, String> {
    @GetMapping("/find-by-code")
    BaseResponse<Optional<DocumentType>> findByCode(@RequestParam String code);
}
