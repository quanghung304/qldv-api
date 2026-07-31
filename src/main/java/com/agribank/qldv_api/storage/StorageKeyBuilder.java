package com.agribank.qldv_api.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh S3 object key theo quy ước dùng chung — mọi key PHẢI có tiền tố {@code s3.project-prefix}.
 * {yyyy}/{MM} lấy từ ngày TẠO hồ sơ/template, KHÔNG lấy ngày hiện tại của thao tác (để cùng 1 hồ
 * sơ/template luôn map về đúng 1 thư mục theo thời gian, bất kể lúc nào bị merge/re-upload lại).
 */
@Component
@RequiredArgsConstructor
public class StorageKeyBuilder {
    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM");

    private final StorageProperties storageProperties;

    public String templateKey(String templateId, String filename, LocalDate createdAt) {
        return "%s/templates/%s/%s/%s/%s".formatted(
                storageProperties.getProjectPrefix(), createdAt.format(YEAR), createdAt.format(MONTH),
                templateId, filename);
    }

    public String draftDocumentKey(String caseId, String templateCode, Timestamp caseCreatedAt) {
        LocalDate createdAt = caseCreatedAt.toLocalDateTime().toLocalDate();
        return "%s/case-documents/%s/%s/%s/%s-draft.docx".formatted(
                storageProperties.getProjectPrefix(), createdAt.format(YEAR), createdAt.format(MONTH),
                caseId, templateCode);
    }
}
