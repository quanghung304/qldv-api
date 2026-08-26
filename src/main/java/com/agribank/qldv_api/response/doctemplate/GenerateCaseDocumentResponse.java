package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCaseDocumentResponse {
    private String templateId;
    private String templateName;
    /** FE gọi GET để tải file vừa sinh (proxy S3, không phải presigned URL). */
    private String draftDownloadPath;
    /** true nếu còn ít nhất 1 placeholder giữ nguyên "[ten_field]" do provider không biết field đó (không có trong Map trả về). */
    private boolean hasUnresolvedFields;
}
