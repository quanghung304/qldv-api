package com.agribank.qldv_api.service.doctemplate;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.DocumentTemplateClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.doctemplate.PlaceholderMappingItemRequest;
import com.agribank.qldv_api.request.doctemplate.UploadDocumentTemplateRequest;
import com.agribank.qldv_api.response.doctemplate.DocumentTemplateResponse;
import com.agribank.qldv_api.response.doctemplate.PlaceholderMappingResponse;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldv_api.storage.S3Service;
import com.agribank.qldv_api.storage.StorageKeyBuilder;
import com.agribank.qldvutils.dto.doctemplate.FieldConfigEntry;
import com.agribank.qldvutils.dto.doctemplate.TemplateMappingConfig;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.DocumentTemplate;
import com.agribank.qldvutils.enums.ERoleCode;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.doctemplate.DocumentTemplatePersistRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Admin API quản lý mẫu văn bản (FN10 chưa seed permission — dùng tạm hardcode role R-ADM thay
 * vì {@code @RequirePermission}, xem {@link #requireAdmin()}).
 *
 * PMDV_DOCUMENT_TYPE/PMDV_DOCUMENT_RULE KHÔNG còn dùng nữa (xem PMDV_DOCUMENT_TEMPLATE) — mọi
 * thông tin "văn bản nào dùng cho hồ sơ loại gì, bước nào" nay nằm thẳng trên
 * {@link DocumentTemplate} (caseTypeId/authorityLevel/workflowStage/templateCode/conditionKey), không
 * cần bảng rule riêng để "khai báo bắt buộc" nữa.
 *
 * field_mapping_config schema v2 — xem {@link TemplateMappingConfig}. Auto-match lúc upload tra
 * {@link FieldCatalog} để gán {@code resolutionType} (SIMPLE/DERIVED/EXTERNAL_LOOKUP) cho từng
 * placeholder; placeholder không auto-match được lưu với {@code resolutionType=null}, chờ admin
 * gán thủ công qua {@link #updateMapping}.
 *
 * Cả upload lẫn update-mapping đều ghi PMDV_DOCUMENT_TEMPLATE + (có thể) chuyển các bản ACTIVE
 * khác cùng "template family" về INACTIVE — 2+ dòng liên quan trong 1 request, nên PHẢI gói thành 1
 * lệnh {@code documentTemplateClient.persist(...)} chạy 1 transaction ở qldv-db
 * (coding-convention.md mục 9), KHÔNG gọi rời rạc nhiều lệnh save/update.
 */
@Service
@RequiredArgsConstructor
public class DocumentTemplateService {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final DocumentTemplateClient documentTemplateClient;
    private final CaseTypeClient caseTypeClient;
    private final S3Service s3Service;
    private final StorageKeyBuilder storageKeyBuilder;
    private final UserService userService;
    private final ObjectMapper objectMapper;

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

        Set<String> placeholderKeys = extractPlaceholders(file);
        List<FieldConfigEntry> fields = buildFieldEntries(placeholderKeys);

        TemplateMappingConfig config = new TemplateMappingConfig();
        config.setFields(fields);

        String filename = file.getOriginalFilename();
        String storageObject = caseType.get().getCode() + authorityLevel + workflowStage + templateCode;

        String storageKey = storageKeyBuilder.templateKey(storageObject, filename, LocalDate.now());
        s3Service.uploadObject(storageKey, readBytes(file), DOCX_CONTENT_TYPE);

        template.setTemplateName(filename);
        template.setStoragePath(storageKey);
        template.setStatus(STATUS_ACTIVE);
        template.setFieldMappingConfig(writeJson(config));

        template = documentTemplateClient.save(template).getData();
        return toResponse(template, fields);
    }

    public DocumentTemplateResponse updateMapping(String templateId, List<PlaceholderMappingItemRequest> manualMappings) {
        requireAdmin();
        DocumentTemplate template = documentTemplateClient.findById(templateId).getData().orElse(null);
        if (template == null) {
            throw new NotFoundException("Không tìm thấy mẫu văn bản");
        }

        TemplateMappingConfig config = readConfig(template.getFieldMappingConfig());
        Map<String, String> manualByPlaceholder = manualMappings.stream()
                .collect(Collectors.toMap(PlaceholderMappingItemRequest::getPlaceholder,
                        PlaceholderMappingItemRequest::getFieldPath, (a, b) -> b));

        for (FieldConfigEntry field : config.getFields()) {
            String manualFieldPath = manualByPlaceholder.get(field.getPlaceholder());
            if (manualFieldPath == null) {
                continue;
            }
            boolean validFieldPath = FieldCatalog.ENTRIES.stream()
                    .anyMatch(e -> FieldCatalog.SIMPLE.equals(e.resolutionType()) && manualFieldPath.equals(e.fieldPath()));
            if (!validFieldPath) {
                throw new CommonException("field_path '" + manualFieldPath + "' không thuộc FieldPathRegistry (SIMPLE)");
            }
            field.setResolutionType(FieldCatalog.SIMPLE);
            field.setFieldPath(manualFieldPath);
            field.setResolverId(null);
            field.setResolverParams(null);
        }

        boolean allMatched = !config.getFields().isEmpty()
                && config.getFields().stream().allMatch(f -> f.getResolutionType() != null);
        boolean becameActive = allMatched && !STATUS_ACTIVE.equals(template.getStatus());
        if (becameActive) {
            template.setStatus(STATUS_ACTIVE);
        }
        template.setFieldMappingConfig(writeJson(config));

        DocumentTemplatePersistRequest persistRequest = new DocumentTemplatePersistRequest();
        persistRequest.setTemplate(template);
        persistRequest.setDeactivateSiblings(becameActive);

        DocumentTemplate saved = documentTemplateClient.persist(persistRequest).getData();
        return toResponse(saved, config.getFields());
    }

    public DocumentTemplateResponse getById(String id) {
        requireAdmin();
        DocumentTemplate template = documentTemplateClient.findById(id).getData().orElse(null);
        if (template == null) {
            throw new NotFoundException("Không tìm thấy mẫu văn bản");
        }
        return toResponse(template, readConfig(template.getFieldMappingConfig()).getFields());
    }

    public List<DocumentTemplateResponse> search(String caseTypeId, Integer authorityLevel, String workflowStage,
                                                  String templateCode, String status) {
        requireAdmin();
        List<DocumentTemplate> templates = safeList(
                documentTemplateClient.search(caseTypeId, authorityLevel, workflowStage, templateCode, status).getData());
        return templates.stream()
                .map(t -> toResponse(t, readConfig(t.getFieldMappingConfig()).getFields()))
                .toList();
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

    private Set<String> extractPlaceholders(MultipartFile file) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        try (InputStream is = file.getInputStream(); XWPFDocument document = new XWPFDocument(is)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                Matcher matcher = FieldCatalog.PLACEHOLDER_PATTERN.matcher(paragraph.getText());
                while (matcher.find()) {
                    keys.add(matcher.group(1));
                }
            }
        } catch (IOException e) {
            throw new CommonException("Không đọc được file .docx, vui lòng kiểm tra lại file upload");
        }
        return keys;
    }

    private List<FieldConfigEntry> buildFieldEntries(Set<String> placeholderKeys) {
        return placeholderKeys.stream()
                .map(key -> {
                    FieldCatalog.Entry entry = FieldCatalog.findByKey(key);
                    if (entry == null) {
                        return new FieldConfigEntry(key, null, null, null, null);
                    }
                    return new FieldConfigEntry(key, entry.resolutionType(), entry.fieldPath(),
                            entry.resolverId(), entry.resolverParams());
                })
                .toList();
    }

    private DocumentTemplateResponse toResponse(DocumentTemplate template, List<FieldConfigEntry> fields) {
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
        response.setPlaceholders(fields.stream()
                .map(f -> new PlaceholderMappingResponse(f.getPlaceholder(), f.getResolutionType(),
                        f.getFieldPath(), f.getResolverId(), f.getResolverParams()))
                .toList());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }

    private TemplateMappingConfig readConfig(String json) {
        if (json == null || json.isBlank()) {
            return new TemplateMappingConfig();
        }
        try {
            return objectMapper.readValue(json, TemplateMappingConfig.class);
        } catch (JsonProcessingException e) {
            throw new CommonException("field_mapping_config lưu trong DB bị lỗi định dạng JSON");
        }
    }

    private String writeJson(TemplateMappingConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new CommonException("Không serialize được field_mapping_config");
        }
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
