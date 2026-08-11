package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Attachment;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.attachment.AttachmentSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "attachmentClient", url = "${qldv.database.url}" + "/api/v1/attachment", configuration = DatabaseFeignConfiguration.class)
public interface AttachmentClient extends BaseClient<Attachment, String> {
    @GetMapping("/find-by-document-ids")
    DefaultListResponse<Attachment> findByDocumentIds(@RequestParam List<String> documentIds);

    @GetMapping("/find-summary-by-case-id")
    DefaultListResponse<AttachmentSummaryResponse> findSummaryByCaseId(@RequestParam String caseId,
                                                                        @RequestParam(required = false) String workflowStage);
}
