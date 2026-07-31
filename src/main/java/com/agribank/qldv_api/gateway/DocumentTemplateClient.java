package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.DocumentTemplate;
import com.agribank.qldvutils.request.doctemplate.DocumentTemplatePersistRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "documentTemplateClient", url = "${qldv.database.url}" + "/api/v1/document-template", configuration = DatabaseFeignConfiguration.class)
public interface DocumentTemplateClient extends BaseClient<DocumentTemplate, String> {
    /** Upload/update-mapping PHẢI gọi qua đây (KHÔNG dùng save() rời rạc) — xem coding-convention.md mục 9. */
    @PostMapping("/persist")
    BaseResponse<DocumentTemplate> persist(@RequestBody DocumentTemplatePersistRequest request);

    /** Toàn bộ version của 1 template family — tính version kế tiếp / xác định bản ACTIVE cũ. */
    @GetMapping("/find-template")
    DefaultResponse<DocumentTemplate> findTemplate(@RequestParam String caseTypeId,
                                                   @RequestParam Integer authorityLevel,
                                                   @RequestParam String workflowStage,
                                                   @RequestParam String templateCode,
                                                   @RequestParam(required = false) String conditionKey);

    /** S2-03 Phần 4 bước 1 — ĐIỂM DUY NHẤT quyết định văn bản nào cần sinh cho 1 bước hồ sơ (thay PMDV_DOCUMENT_RULE cũ). */
    @GetMapping("/find-active-by-stage")
    DefaultListResponse<DocumentTemplate> findActiveByStage(@RequestParam String caseTypeId,
                                                             @RequestParam Integer authorityLevel,
                                                             @RequestParam String workflowStage);

    @GetMapping("/search")
    DefaultListResponse<DocumentTemplate> search(@RequestParam(required = false) String caseTypeId,
                                                  @RequestParam(required = false) Integer authorityLevel,
                                                  @RequestParam(required = false) String workflowStage,
                                                  @RequestParam(required = false) String templateCode,
                                                  @RequestParam(required = false) String status);
}
