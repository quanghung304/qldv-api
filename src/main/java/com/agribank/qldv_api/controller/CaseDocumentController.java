package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.doctemplate.GenerateDocumentsResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.DocumentGenerationService;
import com.agribank.qldvutils.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/generated-documents")
public class CaseDocumentController {
    private final DocumentGenerationService documentGenerationService;

    /**
     * BREAKING CHANGE (S2-03 lần 2): route cũ KHÔNG có {workflowStage} — sửa trực tiếp route này,
     * không giữ song song 2 endpoint. {@code workflowStage} PHẢI khớp status_id hiện tại của hồ
     * sơ (validate trong service), tránh sinh nhầm văn bản của bước khác.
     */
    @Operation(summary = "Sinh tự động toàn bộ văn bản cần cho 1 bước cụ thể của hồ sơ",
            description = "Đọc động PMDV_DOCUMENT_TEMPLATE (status=ACTIVE) theo case_type_id/authority_level/workflowStage, gom theo template_code")
    @RequirePermission(function = "FN1", action = "EDIT")
    @PostMapping("/{caseId}/{workflowStage}")
    public ResponseEntity<BaseResponse<GenerateDocumentsResponse>> generate(@PathVariable String caseId,
                                                                             @PathVariable String workflowStage) {
        return BaseResponse.success(documentGenerationService.generateDocuments(caseId, workflowStage));
    }

    @Operation(summary = "Tải bản draft văn bản đã sinh (không sinh lại)",
            description = "Proxy qua backend, kiểm tra RBAC + scope hồ sơ trước khi đọc S3 — KHÔNG dùng presigned URL")
    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/{caseId}/{templateCode}")
    public ResponseEntity<byte[]> downloadDraft(@PathVariable String caseId, @PathVariable String templateCode) {
        byte[] content = documentGenerationService.downloadDraft(caseId, templateCode);
        String filename = templateCode + "-draft.docx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
