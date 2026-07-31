package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceholderMappingResponse {
    private String placeholder;
    /** SIMPLE / DERIVED / EXTERNAL_LOOKUP, null = chưa auto-match được, chờ admin gán thủ công. */
    private String resolutionType;
    private String fieldPath;
    private String resolverId;
    private Map<String, String> resolverParams;
}
