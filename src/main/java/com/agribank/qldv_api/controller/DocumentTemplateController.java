package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.doctemplate.PlaceholderMappingItemRequest;
import com.agribank.qldv_api.request.doctemplate.UploadDocumentTemplateRequest;
import com.agribank.qldv_api.response.doctemplate.DocumentTemplateResponse;
import com.agribank.qldv_api.service.doctemplate.DocumentTemplateService;
import com.agribank.qldvutils.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin API quản lý mẫu văn bản — CHỈ role R-ADM (kiểm tra trong {@link DocumentTemplateService},
 * FN10 chưa seed permission nên chưa gắn {@code @RequirePermission} như các API khác).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/document-templates")
public class DocumentTemplateController {
    private final DocumentTemplateService documentTemplateService;

    @Operation(summary = "Upload mẫu văn bản .docx",
            description = "Đọc placeholder [ten_field] trong file, đối chiếu FieldCatalog, upload lên S3 và " +
                    "kích hoạt ACTIVE nếu toàn bộ placeholder đã khớp (tự động chuyển các bản ACTIVE khác " +
                    "cùng case_type_id/authority_level/workflow_stage/template_code/condition_key về INACTIVE)")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BaseResponse<DocumentTemplateResponse>> upload(@ModelAttribute UploadDocumentTemplateRequest request) {
        request.validate();
        return BaseResponse.success(documentTemplateService.uploadTemplate(request));
    }

    @Operation(summary = "Gán thủ công field_path cho placeholder chưa khớp FieldCatalog")
    @PutMapping("/{id}/mapping")
    public ResponseEntity<BaseResponse<DocumentTemplateResponse>> updateMapping(
            @PathVariable String id, @RequestBody List<PlaceholderMappingItemRequest> mappings) {
        return BaseResponse.success(documentTemplateService.updateMapping(id, mappings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DocumentTemplateResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(documentTemplateService.getById(id));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<DocumentTemplateResponse>>> search(
            @RequestParam(name = "case_type_id", required = false) String caseTypeId,
            @RequestParam(name = "authority_level", required = false) Integer authorityLevel,
            @RequestParam(name = "workflow_stage", required = false) String workflowStage,
            @RequestParam(name = "template_code", required = false) String templateCode,
            @RequestParam(required = false) String status) {
        return BaseResponse.success(documentTemplateService.search(caseTypeId, authorityLevel, workflowStage, templateCode, status));
    }
}
