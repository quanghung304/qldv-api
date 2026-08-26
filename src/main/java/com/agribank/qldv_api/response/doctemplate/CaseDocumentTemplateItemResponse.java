package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocumentTemplateItemResponse {
    private String templateId;
    private String templateName;
    private String templateCode;
    /** Trả kèm để debug dễ hơn — FE không bắt buộc phải dùng. */
    private String generatorKey;
}
