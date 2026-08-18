package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Map;

/**
 * Cùng tên field với {@code DecisionDocumentsRequest} (thay vì trả nguyên {@code List<Document>}
 * chung chung) để GET/PUT đối chiếu trực tiếp được, không cần tự lọc theo document_name.
 *
 * GC-S3-02-04: {@code missing} rỗng nghĩa là đủ điều kiện và hồ sơ ĐÃ chuyển A-15 → A-16
 * ({@code statusId} phản ánh trạng thái SAU cùng). {@code missing} không rỗng nghĩa là dữ liệu
 * gửi lên đã lưu thành công (response vẫn 200) nhưng CHƯA đủ để tự động chuyển bước — liệt kê
 * đúng phần còn thiếu cho từng bộ văn bản (tinh thần ERR-SC05-03, không phải lỗi chặn lưu).
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
