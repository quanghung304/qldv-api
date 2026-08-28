package com.agribank.qldv_api.service.doctemplate;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.GeneratedDocumentClient;
import com.agribank.qldv_api.gateway.DocumentContentClient;
import com.agribank.qldv_api.gateway.DocumentTemplateClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.OrganizationScope;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldv_api.response.doctemplate.CaseDocumentTemplateItemResponse;
import com.agribank.qldv_api.response.doctemplate.CaseDocumentTemplatesResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateCaseDocumentResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateDocumentResultResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateDocumentsResponse;
import com.agribank.qldv_api.storage.S3Service;
import com.agribank.qldv_api.storage.StorageKeyBuilder;
import com.agribank.qldv_api.workflow.WorkflowAssigneeGuard;
import com.agribank.qldv_api.workflow.WorkflowConditionResolver;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.GeneratedDocument;
import com.agribank.qldvutils.entity.DocumentTemplate;
import com.agribank.qldvutils.enums.Constants;
import com.agribank.qldvutils.enums.EDocumentOrigin;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sinh văn bản (lần 3) — THAY THẾ HOÀN TOÀN {@code DocumentGenerationService} cũ (Resolver
 * Registry/field_mapping_config, đã xoá). Đọc động 100% từ PMDV_DOCUMENT_TEMPLATE, tra giá trị
 * placeholder qua {@link DocumentContentClient} (DocumentContentProvider, generator_key, qldv-db)
 * thay vì field_mapping_config.
 *
 * 3 điểm vào:
 * <ul>
 *   <li>{@link #listAvailableTemplates}: Phần 4 — danh mục biểu mẫu cho hồ sơ ở 1 bước cụ thể;
 *   {@code workflowStage} do caller truyền vào TƯỜNG MINH (query param, KHÔNG tự suy ra từ
 *   {@code case.status_id} hiện tại — cho phép xem/sinh lại văn bản của bước đã qua), lọc theo
 *   {@link WorkflowConditionResolver}.</li>
 *   <li>{@link #generateDraft}: Phần 5 — sinh 1 văn bản cụ thể theo templateId + workflowStage
 *   (cũng truyền tường minh, không tự suy ra); lỗi cấu hình (provider chưa đăng ký, template không
 *   khớp case/workflowStage) NÉM lỗi rõ ràng chặn ngay request này.</li>
 *   <li>{@link #generateAllForStage}: TÁI SỬ DỤNG bởi {@code ArchiveCaseService} (API-SC06-01) —
 *   sinh TOÀN BỘ văn bản ACTIVE của 1 workflowStage trong 1 lượt, mỗi template xử lý độc lập/an
 *   toàn (lỗi 1 template không chặn văn bản khác), giữ đúng hành vi {@code generateDocuments()} cũ
 *   mà ArchiveCaseService phụ thuộc — chỉ đổi cơ chế tra nội dung bên trong.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentContentGenerationService {
    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[([a-zA-Z0-9_]+)]");

    private final CaseClient caseClient;
    private final DocumentTemplateClient documentTemplateClient;
    private final GeneratedDocumentClient generatedDocumentClient;
    private final DocumentContentClient documentContentClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final WorkflowConditionResolver workflowConditionResolver;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;
    private final S3Service s3Service;
    private final StorageKeyBuilder storageKeyBuilder;
    private final BranchNameByBrcdResolver branchNameByBrcdResolver;

    // ---------------------------------------------------------------- Phần 4

    public CaseDocumentTemplatesResponse listAvailableTemplates(String caseId, String workflowStage) {
        verifyCaseAccess(caseId);
        Case caseEntity = requireCase(caseId);

        List<DocumentTemplate> activeTemplates = safeList(documentTemplateClient.findActiveByStage(
                caseEntity.getCaseTypeId(), caseEntity.getAuthorityLevel(), workflowStage).getData());
        String conditionValue = workflowConditionResolver.resolveConditionValue(caseEntity, workflowStage);

        Set<String> generatedTemplateIds = safeList(generatedDocumentClient.findByCaseId(caseId).getData()).stream()
                .map(GeneratedDocument::getTemplateId)
                .collect(Collectors.toSet());

        boolean[] hasHidden = {false};
        List<CaseDocumentTemplateItemResponse> visible = activeTemplates.stream()
                .filter(t -> isVisible(t, conditionValue, hasHidden))
                .map(t -> toItemResponse(t, generatedTemplateIds))
                .toList();

        String warningMessage = hasHidden[0]
                ? "Một số mẫu chưa hiển thị do hồ sơ chưa xác định hình thức xử lý (họp/không họp) của bước liên quan"
                : null;
        return new CaseDocumentTemplatesResponse(visible, hasHidden[0], warningMessage);
    }

    private static boolean isVisible(DocumentTemplate template, String conditionValue, boolean[] hasHidden) {
        if (template.getConditionKey() == null) {
            return true;
        }
        if (conditionValue == null) {
            hasHidden[0] = true;
            return false;
        }
        return template.getConditionKey().equals(conditionValue);
    }

    private static CaseDocumentTemplateItemResponse toItemResponse(DocumentTemplate template, Set<String> generatedTemplateIds) {
        return new CaseDocumentTemplateItemResponse(template.getId(), template.getTemplateName(),
                template.getTemplateCode(), template.getGeneratorKey(), generatedTemplateIds.contains(template.getId()));
    }

    // ---------------------------------------------------------------- Phần 5

    public GenerateCaseDocumentResponse generateDraft(String caseId, String templateId) {
        verifyCaseAccess(caseId);
        Case caseEntity = requireCase(caseId);
        DocumentTemplate template = documentTemplateClient.findById(templateId).getData().orElse(null);
        if (template == null) {
            throw new NotFoundException("Không tìm thấy mẫu văn bản");
        }
        validateTemplateMatchesCase(template, caseEntity);

        Map<String, String> values = resolveContentOrThrow(template, caseId);
        MergeResult merged = mergeAndUpload(caseEntity, template, values);
        String downloadPath = "/api/v1/document-templates/download?caseId=%s&templateId=%s".formatted(caseId, template.getId());
        return new GenerateCaseDocumentResponse(template.getId(), template.getTemplateName(), downloadPath, merged.hasUnresolvedFields());
    }

    public byte[] downloadDraft(String caseId, String templateId) {
        verifyCaseAccess(caseId);
        GeneratedDocument document = generatedDocumentClient.findByCaseIdAndTemplateId(caseId, templateId).getData().orElse(null);
        if (document == null) {
            throw new NotFoundException("Chưa từng sinh văn bản này, vui lòng gọi chức năng sinh văn bản trước");
        }
        Case caseEntity = requireCase(caseId);
        String key = storageKeyBuilder.draftDocumentKey(caseId, document.getTemplateCode(), caseEntity.getCreatedAt());
        return s3Service.downloadObject(key);
    }

    public GeneratedDocument getDocument(String caseId, String templateId) {
        GeneratedDocument document = generatedDocumentClient.findByCaseIdAndTemplateId(caseId, templateId).getData().orElse(null);
        if (document == null) {
            throw new NotFoundException("Chưa từng sinh văn bản này, vui lòng gọi chức năng sinh văn bản trước");
        }
        return document;
    }

    private void validateTemplateMatchesCase(DocumentTemplate template, Case caseEntity) {
        if (!Constants.STATUS_ACTIVE.equals(template.getStatus())) {
            throw new CommonException("Mẫu văn bản '" + template.getTemplateName() + "' không còn hiệu lực (status="
                    + template.getStatus() + ")");
        }
        boolean matches = template.getCaseTypeId().equals(caseEntity.getCaseTypeId())
                && template.getAuthorityLevel().equals(caseEntity.getAuthorityLevel());

        if (!matches) {
            throw new CommonException("Mẫu văn bản '" + template.getTemplateName() + "' không khớp loại hồ sơ/cấp thẩm quyền của hồ sơ này");
        }
    }

    private Map<String, String> resolveContentOrThrow(DocumentTemplate template, String caseId) {
        Map<String, String> values = new LinkedHashMap<>(documentContentClient.resolve(template.getGeneratorKey(), caseId).getData()
                .orElseThrow(() -> new CommonException("Chưa có hàm sinh nội dung cho mẫu '" + template.getTemplateName()
                        + "' (generatorKey='" + template.getGeneratorKey() + "'), vui lòng liên hệ đội phát triển")));
        enrichExternalFields(caseId, values);
        return values;
    }

    /**
     * Field CHỈ tra được ở tầng qldv-api (qldv-db không gọi được IAM/BranchService — đúng ranh giới
     * kiến trúc) — hiện có duy nhất professional_unit_name (tên chi nhánh theo brcd, xem
     * {@link BranchNameByBrcdResolver}). Chỉ put khi tra được giá trị THẬT: key vắng mặt khiến merge
     * giữ nguyên "[professional_unit_name]" gốc, đúng quy ước áp dụng cho mọi field DocumentContentProvider.
     */
    private void enrichExternalFields(String caseId, Map<String, String> values) {
        String branchName = branchNameByBrcdResolver.resolve(caseId);
        if (branchName != null && !branchName.isBlank()) {
            values.put("professional_unit_name", branchName);
        }
    }

    // ---------------------------------------------------------- Bulk theo bước (ArchiveCaseService)

    public GenerateDocumentsResponse generateAllForStage(String caseId, String workflowStage) {
        verifyCaseAccess(caseId);
        Case caseEntity = requireCase(caseId);
        if (!workflowStage.equals(caseEntity.getStatusId())) {
            throw new CommonException("Hồ sơ hiện không ở bước " + caseEntity.getStatusId()
                    + ", không thể sinh văn bản cho bước " + workflowStage);
        }
        workflowAssigneeGuard.requireAssignee(caseEntity, requireUser().getId());

        List<DocumentTemplate> activeTemplates = safeList(documentTemplateClient.findActiveByStage(
                caseEntity.getCaseTypeId(), caseEntity.getAuthorityLevel(), workflowStage).getData());
        if (activeTemplates.isEmpty()) {
            return new GenerateDocumentsResponse(List.of(), "Chưa cấu hình sinh văn bản động cho bước này");
        }

        String conditionValue = workflowConditionResolver.resolveConditionValue(caseEntity, workflowStage);
        Map<String, List<DocumentTemplate>> byTemplateCode = activeTemplates.stream()
                .collect(Collectors.groupingBy(DocumentTemplate::getTemplateCode));

        List<GenerateDocumentResultResponse> results = byTemplateCode.entrySet().stream()
                .map(entry -> processTemplateGroupSafely(caseEntity, entry.getKey(), entry.getValue(), conditionValue))
                .toList();
        return new GenerateDocumentsResponse(results, null);
    }

    /** Độc lập hoàn toàn theo template_code — lỗi bất ngờ ở 1 template không được làm hỏng các template khác trong cùng lượt gọi. */
    private GenerateDocumentResultResponse processTemplateGroupSafely(Case caseEntity, String templateCode,
                                                                        List<DocumentTemplate> candidates, String conditionValue) {
        try {
            return processTemplateGroup(caseEntity, templateCode, candidates, conditionValue);
        } catch (Exception e) {
            log.error("Sinh văn bản lỗi cho template_code={} case_id={}: {}", templateCode, caseEntity.getId(), e.getMessage(), e);
            return new GenerateDocumentResultResponse(null, templateCode, null, "GENERATION_FAILED", false, null, null);
        }
    }

    private GenerateDocumentResultResponse processTemplateGroup(Case caseEntity, String templateCode,
                                                                   List<DocumentTemplate> candidates, String conditionValue) {
        if (candidates.size() == 1 && candidates.get(0).getConditionKey() == null) {
            return generateFromTemplateForResult(caseEntity, candidates.get(0));
        }

        List<DocumentTemplate> withConditionKey = candidates.stream()
                .filter(t -> t.getConditionKey() != null)
                .toList();
        if (withConditionKey.isEmpty()) {
            log.error("Cấu hình template trùng lặp cho template_code={} (case_type_id={}, authority_level={}, workflow_stage={}), cần rà soát",
                    templateCode, caseEntity.getCaseTypeId(), caseEntity.getAuthorityLevel(), caseEntity.getStatusId());
            return new GenerateDocumentResultResponse(null, templateCode, null, "TEMPLATE_NOT_FOUND", false, null, null);
        }

        if (conditionValue == null) {
            return new GenerateDocumentResultResponse(null, templateCode, null, "CONDITION_NOT_SET", false, null,
                    "Hồ sơ chưa xác định hình thức xử lý (họp/không họp) của bước liên quan, vui lòng cập nhật "
                            + "trước khi sinh văn bản cho template " + templateCode);
        }

        List<DocumentTemplate> matched = withConditionKey.stream()
                .filter(t -> t.getConditionKey().equals(conditionValue))
                .toList();
        if (matched.size() != 1) {
            return new GenerateDocumentResultResponse(null, templateCode, null, "TEMPLATE_NOT_FOUND", false, null, null);
        }
        return generateFromTemplateForResult(caseEntity, matched.get(0));
    }

    private GenerateDocumentResultResponse generateFromTemplateForResult(Case caseEntity, DocumentTemplate template) {
        Optional<Map<String, String>> resolved = documentContentClient.resolve(template.getGeneratorKey(), caseEntity.getId()).getData();
        if (resolved.isEmpty()) {
            return new GenerateDocumentResultResponse(template.getId(), template.getTemplateCode(), template.getTemplateName(),
                    "PROVIDER_NOT_FOUND", false, null, "Chưa có hàm sinh nội dung cho mẫu '" + template.getTemplateName()
                    + "' (generatorKey='" + template.getGeneratorKey() + "')");
        }
        Map<String, String> values = new LinkedHashMap<>(resolved.get());
        enrichExternalFields(caseEntity.getId(), values);
        MergeResult merged = mergeAndUpload(caseEntity, template, values);
        String downloadPath = "/api/v1/document-templates/download?caseId=%s&templateId=%s".formatted(caseEntity.getId(), template.getId());
        return new GenerateDocumentResultResponse(template.getId(), template.getTemplateCode(), template.getTemplateName(),
                "GENERATED", merged.hasUnresolvedFields(), downloadPath, null);
    }

    // ---------------------------------------------------------------- helpers dùng chung

    private MergeResult mergeAndUpload(Case caseEntity, DocumentTemplate template, Map<String, String> values) {
        byte[] templateBytes = s3Service.downloadObject(template.getStoragePath());
        MergeResult merged = mergePlaceholders(templateBytes, values);
        String draftKey = storageKeyBuilder.draftDocumentKey(caseEntity.getId(), template.getTemplateCode(), caseEntity.getCreatedAt());
        s3Service.uploadObject(draftKey, merged.content(), DOCX_CONTENT_TYPE);
        upsertGeneratedDocument(caseEntity.getId(), template.getId(), template.getTemplateCode());
        return merged;
    }

    /** Upsert theo (case_id, template_id) — xem comment DocumentRepository.findByCaseIdAndTemplateId. */
    private void upsertGeneratedDocument(String caseId, String templateId, String templateCode) {
        GeneratedDocument document = generatedDocumentClient.findByCaseIdAndTemplateId(caseId, templateId).getData()
                .orElseGet(() -> GeneratedDocument.builder().caseId(caseId).templateId(templateId).build());
        document.setTemplateId(templateId);
        document.setTemplateCode(templateCode);
        document.setOrigin(EDocumentOrigin.GENERATED.getId());
        generatedDocumentClient.save(document);
    }

    private record MergeResult(byte[] content, boolean hasUnresolvedFields) {
    }

    /** Key không có trong values (provider không biết field đó) -> GIỮ NGUYÊN "[ten_field]", không coi là lỗi. */
    private MergeResult mergePlaceholders(byte[] templateBytes, Map<String, String> values) {
        boolean[] hasUnresolved = {false};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(templateBytes);
             XWPFDocument document = new XWPFDocument(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                mergeParagraph(paragraph, values, hasUnresolved);
            }
            for (XWPFTable table : document.getTables()) {
                mergeTable(table, values, hasUnresolved);
            }
            document.write(bos);
            return new MergeResult(bos.toByteArray(), hasUnresolved[0]);
        } catch (IOException e) {
            throw new CommonException("Không merge được file mẫu .docx");
        }
    }

    /** Đệ quy xuống bảng lồng trong ô (cell) — hầu hết placeholder của các mẫu phiếu/biên bản kiểm phiếu nằm trong bảng, không phải paragraph cấp document. */
    private void mergeTable(XWPFTable table, Map<String, String> values, boolean[] hasUnresolved) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    mergeParagraph(paragraph, values, hasUnresolved);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    mergeTable(nestedTable, values, hasUnresolved);
                }
            }
        }
    }

    private void mergeParagraph(XWPFParagraph paragraph, Map<String, String> values, boolean[] hasUnresolved) {
        String original = paragraph.getText();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(original);
        if (!matcher.find()) {
            return;
        }

        int firstPlaceholderOffset = matcher.start();
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

        // Giữ ĐÚNG định dạng gốc (font, cỡ chữ, đậm/nghiêng, màu...) của run chứa placeholder đầu
        // tiên trong file mẫu — PHẢI copy() NGAY tại đây (còn nằm trong cây XML của document đang
        // mở), TRƯỚC khi removeRun() ở dưới xoá run gốc. copy() sau khi remove sẽ ném
        // XmlValueDisconnectedException vì node XML gốc đã bị ngắt kết nối khỏi document.
        // paragraph.createRun() mặc định KHÔNG kế thừa bất kỳ style nào (rơi về mặc định của Word,
        // VD cỡ chữ mẫu là 14 nhưng bị trả về 12 nếu không copy lại thủ công).
        CTRPr templateRunProperties = findRunPropertiesAtOffset(paragraph, firstPlaceholderOffset);
        CTRPr templateRunPropertiesCopy = templateRunProperties == null ? null : (CTRPr) templateRunProperties.copy();

        // Gộp toàn bộ run trong paragraph thành 1 run duy nhất trước khi set text đã thay thế —
        // tránh Word tách run làm placeholder bị thay sai vị trí (kỹ thuật Apache POI đã áp dụng
        // ở DocumentGenerationService cũ, tái sử dụng nguyên vẹn).
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun run = paragraph.createRun();
        if (templateRunPropertiesCopy != null) {
            run.getCTR().setRPr(templateRunPropertiesCopy);
        }
        run.setText(replaced.toString());
    }

    /** Run chứa vị trí ký tự {@code offset} trong {@code paragraph.getText()} — dùng làm "khuôn" định dạng cho run gộp sau khi thay placeholder. */
    private CTRPr findRunPropertiesAtOffset(XWPFParagraph paragraph, int offset) {
        int cursor = 0;
        XWPFRun lastNonEmptyRun = null;
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.text();
            int length = text == null ? 0 : text.length();
            if (length > 0) {
                lastNonEmptyRun = run;
            }
            if (offset < cursor + length) {
                return run.getCTR().getRPr();
            }
            cursor += length;
        }
        return lastNonEmptyRun == null ? null : lastNonEmptyRun.getCTR().getRPr();
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private Case requireCase(String caseId) {
        return caseClient.findById(caseId).getData().orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
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
