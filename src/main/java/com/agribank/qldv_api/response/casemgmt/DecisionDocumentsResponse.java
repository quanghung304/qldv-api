package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Map;

/**
 * Cùng tên field với {@code DecisionDocumentsRequest} (thay vì trả nguyên {@code List<GeneratedDocument>}
 * chung chung) để GET/PUT đối chiếu trực tiếp được, không cần tự lọc theo document_name.
 *
 * {@code missing} CHỈ mang tính thông tin (đã đủ điều kiện để R-CV tự trình kiểm soát — SUBMIT_
 * CONTROL, A-12→A-13 — hay chưa), KHÔNG tự động chuyển trạng thái nữa — {@code statusId} luôn là
 * A-12 (trạng thái hiện tại của hồ sơ, không đổi qua lệnh gọi này). {@code missing} không rỗng
 * nghĩa là dữ liệu gửi lên đã lưu thành công (response vẫn 200) nhưng CHƯA đủ 3 bộ văn bản +
 * scan — liệt kê đúng phần còn thiếu cho từng bộ (tinh thần ERR-SC05-03, không phải lỗi chặn lưu).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DecisionDocumentsResponse {
    String caseId;
    String statusId;

    /** id thật của PMDV_DOCUMENT — dùng để gắn attachment (documentId) qua API riêng. */
    String establishDecisionId;
    String establishDecisionNo;
    LocalDate establishDecisionIssueDate;
    LocalDate establishDecisionEffectiveDate;

    String committeeAppointmentDecisionId;
    String committeeAppointmentDecisionNo;
    LocalDate committeeAppointmentIssueDate;
    LocalDate committeeAppointmentEffectiveDate;

    String politicalStandardConclusionId;
    String politicalStandardConclusionNoFinal;
    LocalDate politicalStandardConclusionIssueDate;
    LocalDate politicalStandardConclusionEffectiveDate;

    Map<String, String> missing;
    Map<String, String> warnings;
}
