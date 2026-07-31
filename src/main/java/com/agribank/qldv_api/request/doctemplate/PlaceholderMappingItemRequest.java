package com.agribank.qldv_api.request.doctemplate;

import lombok.Data;

/** 1 dòng gán thủ công trong PUT /api/v1/document-templates/{id}/mapping. */
@Data
public class PlaceholderMappingItemRequest {
    private String placeholder;
    private String fieldPath;
}
