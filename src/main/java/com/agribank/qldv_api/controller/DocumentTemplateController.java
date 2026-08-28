package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.doctemplate.UploadDocumentTemplateRequest;
import com.agribank.qldv_api.response.doctemplate.CaseDocumentTemplatesResponse;
import com.agribank.qldv_api.response.doctemplate.DocumentTemplateResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateCaseDocumentResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.doctemplate.DocumentContentGenerationService;
import com.agribank.qldv_api.service.doctemplate.DocumentTemplateService;
import com.agribank.qldvutils.entity.GeneratedDocument;
import com.agribank.qldvutils.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Toàn bộ API liên quan mẫu văn bản/sinh văn bản tự động gộp về CHUNG 1 gốc URL
 * {@code /api/v1/document-templates} (đã đổi API contract — trước đây tách 2 controller
 * {@code CaseDocumentTemplateController}/{@code DocumentTemplateController} với 2 prefix khác
 * nhau, đã xoá controller kia và đổi luôn path cho thống nhất, không giữ song song URL cũ):
 * <ul>
 *   <li>Admin quản lý mẫu — {@code /api/v1/document-templates} (upload/search),
 *   {@code /api/v1/document-templates/{id}} (get) — CHỈ role R-ADM (kiểm tra trong
 *   {@link DocumentTemplateService}, FN10 chưa seed permission nên chưa gắn
 *   {@code @RequirePermission} như các API khác).</li>
 *   <li>Sinh văn bản theo hồ sơ (lần 3 — DocumentContentProvider/generator_key) —
 *   {@code GET /api/v1/document-templates/cases/{caseId}?workflowStage=&conditionKey=} (danh mục),
 *   {@code POST /api/v1/document-templates/generate?caseId=&templateId=&workflowStage=} (sinh
 *   draft), {@code GET /api/v1/document-templates/download?caseId=&templateId=} (tải draft).
 *   {@code workflowStage} LUÔN do FE truyền tường minh, KHÔNG tự mặc định lấy bước hiện tại của hồ
 *   sơ. {@code conditionKey} (MEETING/BALLOT) KHÔNG bắt buộc, chỉ dùng để lọc khi FE có truyền —
 *   xem {@link DocumentContentGenerationService#listAvailableTemplates}.
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/document-templates")
public class DocumentTemplateController {
    private static final DateTimeFormatter DOWNLOAD_FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final DocumentTemplateService documentTemplateService;
    private final DocumentContentGenerationService documentContentGenerationService;

    // ---------------------------------------------------------- Admin quản lý mẫu văn bản

    @Operation(summary = "Upload mẫu văn bản .docx",
            description = "Upload file lên S3 và kích hoạt ACTIVE ngay (tự động chuyển các bản ACTIVE khác cùng "
                    + "case_type_id/authority_level/workflow_stage/template_code/condition_key về INACTIVE). "
                    + "generator_key khai báo lúc upload chỉ được thẩm định lúc gọi API sinh draft, không chặn upload.")
    @PostMapping(value = "", consumes = "multipart/form-data")
    public ResponseEntity<BaseResponse<DocumentTemplateResponse>> upload(@ModelAttribute UploadDocumentTemplateRequest request) {
        request.validate();
        return BaseResponse.success(documentTemplateService.uploadTemplate(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DocumentTemplateResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(documentTemplateService.getById(id));
    }

    @GetMapping("")
    public ResponseEntity<BaseResponse<List<DocumentTemplateResponse>>> search(
            @RequestParam(name = "case_type_id", required = false) String caseTypeId,
            @RequestParam(name = "authority_level", required = false) Integer authorityLevel,
            @RequestParam(name = "workflow_stage", required = false) String workflowStage,
            @RequestParam(name = "template_code", required = false) String templateCode,
            @RequestParam(required = false) String status) {
        return BaseResponse.success(documentTemplateService.search(caseTypeId, authorityLevel, workflowStage, templateCode, status));
    }

    // ---------------------------------------------------------- Sinh văn bản theo hồ sơ

    @Operation(summary = "Danh mục biểu mẫu có thể sinh cho hồ sơ ở 1 bước cụ thể",
            description = "Đọc động PMDV_DOCUMENT_TEMPLATE (status=ACTIVE) theo case_type_id/authority_level/workflowStage, "
                    + "lọc thêm theo điều kiện họp/không họp (condition_key) nếu template có khai báo.")
    @RequirePermission(function = "FN6", action = "VIEW")
    @GetMapping("/cases/{caseId}")
    public ResponseEntity<BaseResponse<CaseDocumentTemplatesResponse>> listCaseTemplates(
            @PathVariable String caseId, @RequestParam String workflowStage,
            @RequestParam(required = false) String conditionKey) {
        return BaseResponse.success(documentContentGenerationService.listAvailableTemplates(caseId, workflowStage, conditionKey));
    }

    @Operation(summary = "Sinh bản draft văn bản cho 1 mẫu cụ thể",
            description = "Tra nội dung qua DocumentContentProvider (generator_key), merge vào file .docx mẫu, upload S3, upsert PMDV_DOCUMENT. "
                    + "workflowStage do FE truyền tường minh, dùng để đối chiếu đúng bước với template — KHÔNG tự mặc định lấy bước hiện tại của hồ sơ")
    @RequirePermission(function = "FN6", action = "CREATE")
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<GenerateCaseDocumentResponse>> generateCaseDocument(
            @RequestParam String caseId, @RequestParam String templateId) {
        return BaseResponse.success(documentContentGenerationService.generateDraft(caseId, templateId));
    }

    @Operation(summary = "Tải bản draft văn bản đã sinh (không sinh lại)",
            description = "Proxy qua backend, kiểm tra RBAC + scope hồ sơ trước khi đọc S3 — KHÔNG dùng presigned URL. "
                    + "Tên file tải về có kèm mốc thời gian LÚC TẢI (không phải lúc sinh văn bản)")
    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadCaseDocumentDraft(@RequestParam String caseId, @RequestParam String templateId) {
        GeneratedDocument document = documentContentGenerationService.getDocument(caseId, templateId);
        byte[] content = documentContentGenerationService.downloadDraft(caseId, templateId);
        String timestamp = LocalDateTime.now().format(DOWNLOAD_FILENAME_TIMESTAMP_FORMAT);
        String filename = "%s_%s.docx".formatted(document.getTemplateCode(), timestamp);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
