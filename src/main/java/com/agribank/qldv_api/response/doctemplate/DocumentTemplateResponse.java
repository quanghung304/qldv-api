package com.agribank.qldv_api.response.doctemplate;

import lombok.Data;

import java.sql.Timestamp;

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
    private String status;
    private String generatorKey;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
