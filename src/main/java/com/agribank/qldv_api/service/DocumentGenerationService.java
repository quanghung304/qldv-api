package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.DocumentClient;
import com.agribank.qldv_api.gateway.DocumentTemplateClient;
import com.agribank.qldv_api.gateway.FieldMappingClient;
import com.agribank.qldv_api.response.doctemplate.GenerateDocumentResultResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateDocumentsResponse;
import com.agribank.qldv_api.service.doctemplate.BranchNameByBrcdResolver;
import com.agribank.qldv_api.service.doctemplate.FieldCatalog;
import com.agribank.qldv_api.storage.S3Service;
import com.agribank.qldv_api.storage.StorageKeyBuilder;
import com.agribank.qldvutils.dto.doctemplate.FieldConfigEntry;
import com.agribank.qldvutils.dto.doctemplate.TemplateMappingConfig;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.Document;
import com.agribank.qldvutils.entity.DocumentTemplate;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import com.agribank.qldvutils.enums.EDocumentOrigin;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.doctemplate.ResolveFieldsRequest;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import com.agribank.qldvutils.response.doctemplate.FieldResolutionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * S2-03 (lần 3) — sinh văn bản theo TỪNG BƯỚC, đọc động 100% từ PMDV_DOCUMENT_TEMPLATE (thay hoàn
 * toàn PMDV_DOCUMENT_RULE đã bỏ — GC-08). Service này CHỈ ĐỌC PMDV_DOCUMENT_TEMPLATE — không bao
 * giờ tự ghi status/kích hoạt (nơi ghi duy nhất là DocumentTemplateService khi ADMIN upload/duyệt).
 *
 * Tra giá trị placeholder SIMPLE/DERIVED ủy quyền cho {@link FieldMappingClient} (Resolver
 * Registry ở qldv-db). Riêng EXTERNAL_LOOKUP (hiện chỉ có BRANCH_NAME_BY_BRCD) được resolve NGAY
 * TẠI ĐÂY qua {@link BranchNameByBrcdResolver} — cố ý KHÔNG đặt ở qldv-db vì cần tái sử dụng
 * IAMClient/BranchService (chỉ tồn tại ở qldv-api); qldv-db bỏ qua hoàn toàn field loại này (xem
 * {@code FieldMappingService}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String BRANCH_NAME_BY_BRCD = "BRANCH_NAME_BY_BRCD";

    private final CaseClient caseClient;
    private final DocumentTemplateClient documentTemplateClient;
    private final DocumentClient documentClient;
    private final FieldMappingClient fieldMappingClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final OrganizationService organizationService;
    private final BranchNameByBrcdResolver branchNameByBrcdResolver;
    private final S3Service s3Service;
    private final StorageKeyBuilder storageKeyBuilder;
    private final ObjectMapper objectMapper;

    public GenerateDocumentsResponse generateDocuments(String caseId, String workflowStage) {
        verifyCaseAccess(caseId);
        Case caseEntity = caseClient.findById(caseId).getData().orElse(null);
        if (caseEntity == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }
        if (!workflowStage.equals(caseEntity.getStatusId())) {
            throw new CommonException("Hồ sơ hiện không ở bước " + caseEntity.getStatusId()
                    + ", không thể sinh văn bản cho bước " + workflowStage);
        }

        List<DocumentTemplate> activeTemplates = safeList(documentTemplateClient.findActiveByStage(
                caseEntity.getCaseTypeId(), caseEntity.getAuthorityLevel(), workflowStage).getData());

        if (activeTemplates.isEmpty()) {
            return new GenerateDocumentsResponse(List.of(), "Chưa cấu hình sinh văn bản động cho bước này");
        }

        Map<String, List<DocumentTemplate>> bytemplate = activeTemplates.stream()
                .collect(Collectors.groupingBy(DocumentTemplate::getTemplateCode));

        List<GenerateDocumentResultResponse> results = bytemplate.entrySet().stream()
                .map(entry -> processtemplateSafely(caseEntity, entry.getKey(), entry.getValue()))
                .toList();

        return new GenerateDocumentsResponse(results, null);
    }

    public byte[] downloadDraft(String caseId, String templateCode) {
        verifyCaseAccess(caseId);
        Document document = documentClient.findByCaseIdAndTemplateCode(caseId, templateCode).getData().orElse(null);
        if (document == null) {
            throw new NotFoundException("Chưa từng sinh văn bản này, vui lòng gọi chức năng sinh văn bản trước");
        }
        Case caseEntity = caseClient.findById(caseId).getData().orElse(null);
        if (caseEntity == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }
        String key = storageKeyBuilder.draftDocumentKey(caseId, templateCode, caseEntity.getCreatedAt());
        return s3Service.downloadObject(key);
    }

    /** Độc lập hoàn toàn theo template — lỗi bất ngờ ở 1 template không được làm hỏng các template khác trong cùng lượt gọi. */
    private GenerateDocumentResultResponse processtemplateSafely(Case caseEntity, String templateCode, List<DocumentTemplate> candidates) {
        try {
            return processtemplate(caseEntity, templateCode, candidates);
        } catch (Exception e) {
            log.error("Sinh văn bản lỗi cho template_code={} case_id={}: {}", templateCode, caseEntity.getId(), e.getMessage(), e);
            return new GenerateDocumentResultResponse(null, templateCode, null, "GENERATION_FAILED", false, null, null);
        }
    }

    /**
     * Phần 4 bước 2 — 1 record với condition_key=null dùng luôn; nhiều record (hoặc 1 record còn
     * lại có condition_key khác null, trường hợp không nêu tường minh trong prompt nhưng xử lý
     * nhất quán cùng nhánh) BẮT BUỘC lọc theo case.btvMethod.
     */
    private GenerateDocumentResultResponse processtemplate(Case caseEntity, String templateCode, List<DocumentTemplate> candidates) {
        if (candidates.size() == 1 && candidates.get(0).getConditionKey() == null) {
            return generateFromTemplate(caseEntity, candidates.get(0));
        }

        List<DocumentTemplate> withConditionKey = candidates.stream()
                .filter(t -> t.getConditionKey() != null)
                .toList();
        if (withConditionKey.isEmpty()) {
            log.error("Cấu hình template trùng lặp cho template {} (case_type_id={}, authority_level={}, workflow_stage={}), cần rà soát",
                    templateCode, caseEntity.getCaseTypeId(), caseEntity.getAuthorityLevel(), caseEntity.getStatusId());
            return new GenerateDocumentResultResponse(null, templateCode, null, "TEMPLATE_NOT_FOUND", false, null, null);
        }

        if (caseEntity.getBtvMethod() == null) {
            return new GenerateDocumentResultResponse(null, templateCode, null, "BTV_METHOD_NOT_SET", false, null,
                    "Hồ sơ chưa xác định hình thức xử lý của Ban Thường vụ (họp/lấy ý kiến bằng phiếu), "
                            + "vui lòng cập nhật trước khi sinh văn bản cho template " + templateCode);
        }

        String methodName = resolveMethodName(caseEntity.getBtvMethod());
        List<DocumentTemplate> matched = withConditionKey.stream()
                .filter(t -> t.getConditionKey().equals(methodName))
                .toList();
        if (matched.size() != 1) {
            return new GenerateDocumentResultResponse(null, templateCode, null, "TEMPLATE_NOT_FOUND", false, null, null);
        }
        return generateFromTemplate(caseEntity, matched.get(0));
    }

    private static String resolveMethodName(Integer btvMethod) {
        if (btvMethod == null) {
            return null;
        }
        for (EBoardReviewMethod method : EBoardReviewMethod.values()) {
            if (method.matches(btvMethod)) {
                return method.name();
            }
        }
        return null;
    }

    private GenerateDocumentResultResponse generateFromTemplate(Case caseEntity, DocumentTemplate template) {
        FieldResolutionResult resolution = fieldMappingClient
                .resolveFields(new ResolveFieldsRequest(caseEntity.getId(), template.getFieldMappingConfig()))
                .getData();
        if (resolution == null) {
            return new GenerateDocumentResultResponse(template.getId(), template.getTemplateCode(), template.getTemplateName(),
                    "MAPPING_INCOMPLETE", false, null, null);
        }

        Map<String, String> values = new HashMap<>(resolution.getValues());
        resolveExternalLookupFields(caseEntity.getId(), template.getFieldMappingConfig(), values);

        byte[] templateBytes = s3Service.downloadObject(template.getStoragePath());
        MergeResult merged = mergePlaceholders(templateBytes, values);

        String draftKey = storageKeyBuilder.draftDocumentKey(caseEntity.getId(), template.getTemplateCode(), caseEntity.getCreatedAt());
        s3Service.uploadObject(draftKey, merged.content(), DOCX_CONTENT_TYPE);

        upsertGeneratedDocument(caseEntity.getId(), template.getId(), template.getTemplateCode());

        String downloadPath = "/api/v1/cases/%s/documents/%s/draft".formatted(caseEntity.getId(), template.getTemplateCode());
        return new GenerateDocumentResultResponse(template.getId(), template.getTemplateCode(), template.getTemplateName(),
                "GENERATED", merged.hasUnresolvedFields(), downloadPath, null);
    }

    /**
     * qldv-db (FieldMappingService) cố ý bỏ qua field EXTERNAL_LOOKUP — tự parse lại
     * field_mapping_config ở đây để tìm đúng các field đó và resolve bằng
     * {@link BranchNameByBrcdResolver} (dùng IAMClient/BranchService), rồi merge kết quả vào
     * {@code values}. resolver_id không phải BRANCH_NAME_BY_BRCD (chưa từng khai báo) -> coi là lỗi
     * cấu hình, trả về false để cả document bị đánh dấu MAPPING_INCOMPLETE — nhất quán với cách
     * qldv-db xử lý resolver_id không hợp lệ.
     */
    private void resolveExternalLookupFields(String caseId, String fieldMappingConfigJson, Map<String, String> values) {
        TemplateMappingConfig config = parseConfig(fieldMappingConfigJson);
        for (FieldConfigEntry field : config.getFields()) {
            if (!FieldCatalog.EXTERNAL_LOOKUP.equals(field.getResolutionType())) {
                continue;
            }
            if (!BRANCH_NAME_BY_BRCD.equals(field.getResolverId())) {
                log.warn("field_mapping_config: resolver_id '{}' (placeholder '{}') không có ở qldv-api",
                        field.getResolverId(), field.getPlaceholder());
                return;
            }
            values.put(field.getPlaceholder(), branchNameByBrcdResolver.resolve(caseId));
        }
    }

    private TemplateMappingConfig parseConfig(String json) {
        if (json == null || json.isBlank()) {
            return new TemplateMappingConfig();
        }
        try {
            return objectMapper.readValue(json, TemplateMappingConfig.class);
        } catch (JsonProcessingException e) {
            throw new CommonException("field_mapping_config lưu trong DB bị lỗi định dạng JSON");
        }
    }

    /** Upsert theo (case_id, template_code) — template_code ổn định qua các version template khác nhau, KHÔNG dùng template_id làm khóa. */
    private void upsertGeneratedDocument(String caseId, String templateId, String templateCode) {
        Document document = documentClient.findByCaseIdAndTemplateCode(caseId, templateCode).getData()
                .orElseGet(() -> Document.builder().caseId(caseId).templateCode(templateCode).build());
        document.setTemplateId(templateId);
        document.setOrigin(EDocumentOrigin.GENERATED.getId());
        documentClient.save(document);
    }

    private record MergeResult(byte[] content, boolean hasUnresolvedFields) {
    }

    /** Placeholder không có trong values (kể cả value null — không có dữ liệu cho hồ sơ này) -> GIỮ NGUYÊN "[ten_field]", không coi là lỗi. */
    private MergeResult mergePlaceholders(byte[] templateBytes, Map<String, String> values) {
        boolean[] hasUnresolved = {false};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                mergeParagraph(paragraph, values, hasUnresolved);
            }
            document.write(bos);
            return new MergeResult(bos.toByteArray(), hasUnresolved[0]);
        } catch (IOException e) {
            throw new CommonException("Không merge được file mẫu .docx");
        }
    }

    private void mergeParagraph(XWPFParagraph paragraph, Map<String, String> values, boolean[] hasUnresolved) {
        String original = paragraph.getText();
        Matcher matcher = FieldCatalog.PLACEHOLDER_PATTERN.matcher(original);
        if (!matcher.find()) {
            return;
        }

        matcher.reset();
        StringBuilder replaced = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            replaced.append(original, last, matcher.start());
            String value = values.get(matcher.group(1));
            if (value == null) {
                hasUnresolved[0] = true;
                replaced.append(matcher.group(0));
            } else {
                replaced.append(value);
            }
            last = matcher.end();
        }
        replaced.append(original.substring(last));

        // Gộp toàn bộ run trong paragraph thành 1 run duy nhất trước khi set text đã thay thế —
        // tránh Word tách run làm placeholder bị thay sai vị trí (yêu cầu kỹ thuật trong prompt).
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun run = paragraph.createRun();
        run.setText(replaced.toString());
    }

    private void verifyCaseAccess(String caseId) {
        OrganizationScope scope = organizationService.resolveScope();
        if (scope.isFull()) {
            return;
        }
        List<CaseOrganizationSummaryResponse> orgLinks = safeList(
                caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());
        boolean allowed = orgLinks.stream().anyMatch(link -> scope.isAllowed(link.getOrganizationId()));
        if (!allowed) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền truy cập hồ sơ này");
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
