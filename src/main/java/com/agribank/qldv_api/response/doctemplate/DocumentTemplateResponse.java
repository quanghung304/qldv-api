package com.agribank.qldv_api.response.doctemplate;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
public class DocumentTemplateResponse {
    private String id;
    private String caseTypeId;
    private Integer authorityLevel;
    private String workflowStage;
    private String templateCode;
    private String conditionKey;
    private String templateName;
    private String storagePath;
    private Integer version;
    private String status;
    private LocalDate effectiveDate;
    private List<PlaceholderMappingResponse> placeholders;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
