package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocumentTemplatesResponse {
    private List<CaseDocumentTemplateItemResponse> templates;
    /** true khi có ít nhất 1 template bị ẩn do chưa xác định được điều kiện họp/không họp của bước hiện tại. */
    private boolean hasHiddenTemplates;
    /** Chỉ có giá trị khi hasHiddenTemplates=true. */
    private String warningMessage;
}
