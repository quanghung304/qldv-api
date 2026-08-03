package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.attachment.UploadAttachmentsRequest;
import com.agribank.qldv_api.response.attachment.AttachmentUploadResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.AttachmentService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.attachment.AttachmentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * API-FN5 — đính kèm tài liệu (docs/domain/api-conventions.md mục "Đính kèm & sinh văn bản").
 * Mọi API tải file PROXY qua backend (kiểm tra RBAC + scope rồi mới đọc S3), KHÔNG dùng presigned URL.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/attachments")
public class AttachmentController {
    private static final Map<String, String> CONTENT_TYPE_BY_EXTENSION = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    private final AttachmentService attachmentService;

    @Value("${attachment.max-size}")
    private long maxTotalSizeKb;

    @Operation(summary = "Đính kèm tài liệu cho hồ sơ (THAY THẾ, không cộng dồn)",
            description = "Toàn bộ tài liệu cũ thuộc đúng (case_id, workflow_stage hiện tại) bị thay thế hoàn toàn bởi danh sách file mới")
    @RequirePermission(function = "FN5", action = "CREATE")
    @PostMapping(value = "/{caseId}", consumes = "multipart/form-data")
    public ResponseEntity<BaseResponse<AttachmentUploadResponse>> upload(
            @PathVariable String caseId, @ModelAttribute UploadAttachmentsRequest request) {
        request.validate(maxTotalSizeKb);
        return BaseResponse.success(attachmentService.uploadAttachments(caseId, request));
    }

    @Operation(summary = "Danh sách tài liệu đính kèm của hồ sơ",
            description = "Không truyền workflow_stage -> trả toàn bộ tài liệu của hồ sơ, mọi bước")
    @RequirePermission(function = "FN5", action = "VIEW")
    @GetMapping("/{caseId}")
    public ResponseEntity<BaseResponse<List<AttachmentSummaryResponse>>> list(
            @PathVariable String caseId,
            @RequestParam(name = "workflow_stage", required = false) String workflowStage) {
        return BaseResponse.success(attachmentService.listAttachments(caseId, workflowStage));
    }

    @Operation(summary = "Tải/xem tài liệu đính kèm",
            description = "Proxy qua backend, kiểm tra RBAC + scope theo case chứa attachment trước khi đọc S3 — KHÔNG dùng presigned URL")
    @RequirePermission(function = "FN5", action = "VIEW")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        AttachmentService.AttachmentDownload download = attachmentService.downloadAttachment(id);
        return ResponseEntity.ok()
                .contentType(resolveContentType(download.fileName()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(download.fileName()).build().toString())
                .body(download.content());
    }

    @Operation(summary = "Xóa tài liệu đính kèm",
            description = "Chỉ cho phép khi hồ sơ chứa tài liệu này CHƯA hoàn thành")
    @RequirePermission(function = "FN5", action = "DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable String id) {
        attachmentService.deleteAttachment(id);
        return BaseResponse.success("Success");
    }

    private static MediaType resolveContentType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String extension = dotIndex < 0 ? "" : fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return MediaType.parseMediaType(
                CONTENT_TYPE_BY_EXTENSION.getOrDefault(extension, "application/octet-stream"));
    }
}
