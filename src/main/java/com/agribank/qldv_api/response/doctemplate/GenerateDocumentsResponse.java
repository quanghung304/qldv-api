package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDocumentsResponse {
    private List<GenerateDocumentResultResponse> results;
    /** VD "Bước hiện tại không yêu cầu sinh văn bản tự động" khi results rỗng — không coi là lỗi. */
    private String message;
}
