package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDocumentResultResponse {
    /** null khi status không xác định được template cụ thể (VD BTV_METHOD_NOT_SET, TEMPLATE_NOT_FOUND do cấu hình trùng). */
    private String templateId;
    private String templateCode;
    private String templateName;
    /** GENERATED / TEMPLATE_NOT_FOUND / MAPPING_INCOMPLETE / BTV_METHOD_NOT_SET / GENERATION_FAILED (bổ sung — lỗi bất ngờ, xem báo cáo cuối) */
    private String status;
    /** true nếu còn ít nhất 1 placeholder giữ nguyên "[ten_field]" do resolver trả về null (không có dữ liệu cho hồ sơ này). Chỉ có ý nghĩa khi status=GENERATED. */
    private Boolean hasUnresolvedFields;
    /** Chỉ có giá trị khi status = GENERATED. */
    private String draftDownloadPath;
    /** Thông báo bổ sung cho FE (VD lý do BTV_METHOD_NOT_SET) — null nếu không cần giải thích thêm. */
    private String message;
}
