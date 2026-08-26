package com.agribank.qldv_api.service.doctemplate;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.DocumentTemplateClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.doctemplate.UploadDocumentTemplateRequest;
import com.agribank.qldv_api.response.doctemplate.DocumentTemplateResponse;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldv_api.storage.S3Service;
import com.agribank.qldv_api.storage.StorageKeyBuilder;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.DocumentTemplate;
import com.agribank.qldvutils.enums.Constants;
import com.agribank.qldvutils.enums.ERoleCode;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.doctemplate.DocumentTemplatePersistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Admin API quản lý mẫu văn bản (FN10 chưa seed permission — dùng tạm hardcode role R-ADM thay vì
 * {@code @RequirePermission}, xem {@link #requireAdmin()}).
 *
 * Sinh văn bản lần 3 — mỗi template gắn 1 {@code generator_key} (gõ tay lúc upload, khớp tên bean
 * {@code DocumentContentProvider} ở qldv-db) THAY THẾ HOÀN TOÀN cơ chế auto-match placeholder theo
 * field_mapping_config (schema v2, đã xoá cùng {@code FieldCatalog}/{@code TemplateMappingConfig})
 * — không còn khái niệm "chưa khớp hết placeholder, chờ admin gán tay" nên upload LUÔN kích hoạt
 * ACTIVE ngay (không còn trạng thái PENDING_REVIEW chờ mapping); {@code generator_key} không khớp
 * bean nào chỉ báo lỗi LÚC GỌI API sinh draft (xem {@code DocumentContentGenerationService}), không
 * chặn upload — vì thứ tự thực tế có thể là đăng ký template trước, dev viết hàm Java sau.
 *
 * Upload vẫn ghi PMDV_DOCUMENT_TEMPLATE + chuyển các bản ACTIVE khác cùng "template family" về
 * INACTIVE trong CÙNG 1 transaction (coding-convention.md mục 9), KHÔNG gọi save() rời rạc.
 */
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final DocumentTemplateClient documentTemplateClient;
    private final CaseTypeClient caseTypeClient;
    private final S3Service s3Service;
    private final StorageKeyBuilder storageKeyBuilder;
    private final UserService userService;

    public DocumentTemplateResponse uploadTemplate(UploadDocumentTemplateRequest request) {
        requireAdmin();

        Optional<CaseType> caseType = caseTypeClient.findById(request.getCaseTypeId()).getData();
        if (caseType.isEmpty()) {
            throw new NotFoundException("Không tìm thấy loại hồ sơ");
        }

        MultipartFile file = request.getFile();
        String caseTypeId = request.getCaseTypeId();
        Integer authorityLevel = request.getAuthorityLevel();
        String workflowStage = request.getWorkflowStage();
        String templateCode = request.getTemplateCode();
        String conditionKey = request.getConditionKey();

        DocumentTemplate template = documentTemplateClient.findTemplate(caseTypeId, authorityLevel, workflowStage, templateCode, conditionKey).getData();
        if (Objects.isNull(template)) {
            template = DocumentTemplate.builder()
                    .caseTypeId(caseTypeId)
                    .authorityLevel(authorityLevel)
                    .workflowStage(workflowStage)
                    .templateCode(templateCode)
                    .conditionKey(conditionKey)
                    .build();
        }

        String filename = file.getOriginalFilename();
        String storageObject = caseType.get().getCode() + authorityLevel + workflowStage + templateCode;
        String storageKey = storageKeyBuilder.templateKey(storageObject, filename, LocalDate.now());
        s3Service.uploadObject(storageKey, readBytes(file), DOCX_CONTENT_TYPE);

        template.setTemplateName(filename);
        template.setStoragePath(storageKey);
        template.setGeneratorKey(request.getGeneratorKey());
        template.setStatus(Constants.STATUS_ACTIVE);

        DocumentTemplatePersistRequest persistRequest = new DocumentTemplatePersistRequest();
        persistRequest.setTemplate(template);
        persistRequest.setDeactivateSiblings(true);

        DocumentTemplate saved = documentTemplateClient.persist(persistRequest).getData();
        return toResponse(saved);
    }

    public DocumentTemplateResponse getById(String id) {
        requireAdmin();
        DocumentTemplate template = documentTemplateClient.findById(id).getData().orElse(null);
        if (template == null) {
            throw new NotFoundException("Không tìm thấy mẫu văn bản");
        }
        return toResponse(template);
    }

    public List<DocumentTemplateResponse> search(String caseTypeId, Integer authorityLevel, String workflowStage,
                                                  String templateCode, String status) {
        requireAdmin();
        List<DocumentTemplate> templates = safeList(
                documentTemplateClient.search(caseTypeId, authorityLevel, workflowStage, templateCode, status).getData());
        return templates.stream().map(this::toResponse).toList();
    }

    private void requireAdmin() {
        UserDetailsImpl userRequested = userService.getUserRequested();
        if (userRequested == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        List<String> roleCodes = userRequested.getRoleCodes();
        if (roleCodes == null || !roleCodes.contains(ERoleCode.R_ADM.getCode())) {
            throw new ForbiddenException("ERR-GL-02: Chỉ role R-ADM được phép quản lý danh mục văn bản");
        }
    }

    private DocumentTemplateResponse toResponse(DocumentTemplate template) {
        DocumentTemplateResponse response = new DocumentTemplateResponse();
        response.setId(template.getId());
        response.setCaseTypeId(template.getCaseTypeId());
        response.setAuthorityLevel(template.getAuthorityLevel());
        response.setWorkflowStage(template.getWorkflowStage());
        response.setTemplateCode(template.getTemplateCode());
        response.setConditionKey(template.getConditionKey());
        response.setTemplateName(template.getTemplateName());
        response.setStoragePath(template.getStoragePath());
        response.setStatus(template.getStatus());
        response.setGeneratorKey(template.getGeneratorKey());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new CommonException("Không đọc được nội dung file upload");
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
