package com.agribank.qldv_api.request.doctemplate;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldvutils.enums.EAuthorityLevel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Toàn bộ tham số POST /api/v1/document-templates gộp thành 1 request (multipart/form-data —
 * {@link MultipartFile} + field thường bind chung qua {@code @ModelAttribute}, cùng cách với
 * {@code CaseSearchRequest}/{@code EstablishmentCaseRequest} cho JSON body).
 *
 * BỎ {@code documentTypeId} (PMDV_DOCUMENT_TYPE không còn dùng) — thay bằng {@code templateCode}
 * (định danh tự do do người upload đặt, KHÔNG tra danh mục), {@code conditionKey} (nullable, rẽ
 * nhánh khi 1 template có nhiều template cùng bước), {@code templateName} (tên hiển thị tự do).
 *
 * {@link #validate()} CHỈ gồm phần KHÔNG cần truy vấn DB (định dạng file, độ dài, field bắt buộc,
 * authority_level/workflow_stage khớp enum cố định sẵn có trong code) — gọi ở controller TRƯỚC
 * khi vào service. Phần validate CẦN DB (case_type_id có tồn tại danh mục hay không) vẫn nằm ở
 * {@code DocumentTemplateService}.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadDocumentTemplateRequest {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final int MAX_TEMPLATE_NAME_LENGTH = 255;

    MultipartFile file;
    String caseTypeId;
    Integer authorityLevel;
    String workflowStage;
    String templateCode;
    String conditionKey;
    /** Định danh hàm Java sinh nội dung (PMDV_DOCUMENT_TEMPLATE.generator_key) — gõ tay, chưa cần khớp bean nào đã có sẵn (xem entity DocumentTemplate). */
    String generatorKey;

    public void validate() {
        Map<String, String> errors = new LinkedHashMap<>();

        if (file == null || file.isEmpty()) {
            errors.put("file", "File mẫu không được để trống");
        } else {
            if (file.getSize() > Constants.MAX_FILE_SIZE) {
                errors.put("file", "File mẫu vượt quá dung lượng cho phép");
            }
            String originalName = file.getOriginalFilename();
            if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".docx")) {
                errors.put("file", "Tên file/định dạng không hợp lệ — chỉ chấp nhận .docx");
            }
            if (!DOCX_CONTENT_TYPE.equals(file.getContentType())) {
                errors.put("file", "Định dạng file không hợp lệ — chỉ chấp nhận .docx");
            }
        }
        if (caseTypeId == null || caseTypeId.isBlank()) {
            errors.put("caseTypeId", "caseTypeId không được để trống");
        }
        if (authorityLevel == null || EAuthorityLevel.getLabel(authorityLevel) == null) {
            errors.put("authorityLevel", "authorityLevel phải là 1 (BANK_LEVEL) hoặc 2 (GRASSROOTS_LEVEL)");
        }
        if (workflowStage == null || workflowStage.isBlank()) {
            errors.put("workflowStage", "workflowStage không được để trống");
        } else {
            try {
                ECaseStatusCode.fromCode(workflowStage);
            } catch (IllegalArgumentException e) {
                errors.put("workflowStage", "workflowStage không hợp lệ: " + workflowStage);
            }
        }
        if (templateCode == null || templateCode.isBlank()) {
            errors.put("templateCode", "templateCode không được để trống");
        }
        if (generatorKey == null || generatorKey.isBlank()) {
            errors.put("generatorKey", "generatorKey không được để trống");
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}
