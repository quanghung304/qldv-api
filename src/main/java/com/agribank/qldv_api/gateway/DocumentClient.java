package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Document;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "documentClient", url = "${qldv.database.url}" + "/api/v1/document", configuration = DatabaseFeignConfiguration.class)
public interface DocumentClient extends BaseClient<Document, String> {
    @GetMapping("/find-by-case-id-and-document-type-id")
    BaseResponse<Optional<Document>> findByCaseIdAndDocumentTypeId(@RequestParam String caseId, @RequestParam String documentTypeId);

    @GetMapping("/exists-by-document-no")
    BaseResponse<Boolean> existsByDocumentNo(@RequestParam String documentNo, @RequestParam(required = false) String excludeId);
}
