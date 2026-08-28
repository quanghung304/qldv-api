package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.GeneratedDocument;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "documentClient", url = "${qldv.database.url}" + "/api/v1/document", configuration = DatabaseFeignConfiguration.class)
public interface GeneratedDocumentClient extends BaseClient<GeneratedDocument, String> {
    @GetMapping("/find-by-case-id")
    BaseResponse<List<GeneratedDocument>> findByCaseId(@RequestParam String caseId);

    @GetMapping("/find-by-case-id-and-template-id")
    BaseResponse<Optional<GeneratedDocument>> findByCaseIdAndTemplateId(@RequestParam String caseId, @RequestParam String templateId);

    @GetMapping("/find-by-case-id-and-document-name")
    BaseResponse<Optional<GeneratedDocument>> findByCaseIdAndDocumentName(@RequestParam String caseId, @RequestParam String documentName);

    @GetMapping("/exists-by-document-no")
    BaseResponse<Boolean> existsByDocumentNo(@RequestParam String documentNo, @RequestParam(required = false) String excludeId);
}
