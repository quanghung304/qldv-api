package com.agribank.qldv_api.response.doctemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDocumentResultResponse {
    /** null khi status không xác định được template cụ thể (VD CONDITION_NOT_SET, TEMPLATE_NOT_FOUND do cấu hình trùng). */
    private String templateId;
    private String templateCode;
    private String templateName;
    /**
     * GENERATED / TEMPLATE_NOT_FOUND (0 hoặc nhiều hơn 1 template khớp condition_key, cấu hình
     * trùng lặp) / CONDITION_NOT_SET (hồ sơ chưa xác định hình thức họp/không họp của bước liên
     * quan — thay BTV_METHOD_NOT_SET cũ, tổng quát hoá cho cả PMDV_CASE_BOARD_REVIEW.method, xem
     * WorkflowConditionResolver) / PROVIDER_NOT_FOUND (generator_key chưa đăng ký
     * DocumentContentProvider nào, thay MAPPING_INCOMPLETE cũ) / GENERATION_FAILED (lỗi bất ngờ).
     */
    private String status;
    /** true nếu còn ít nhất 1 placeholder giữ nguyên "[ten_field]" do resolver trả về null (không có dữ liệu cho hồ sơ này). Chỉ có ý nghĩa khi status=GENERATED. */
    private Boolean hasUnresolvedFields;
    /** Chỉ có giá trị khi status = GENERATED. */
    private String draftDownloadPath;
    /** Thông báo bổ sung cho FE (VD lý do BTV_METHOD_NOT_SET) — null nếu không cần giải thích thêm. */
    private String message;
}
