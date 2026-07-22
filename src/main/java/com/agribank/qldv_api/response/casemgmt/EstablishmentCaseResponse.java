package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Response chung cho API-SC02-01 (201) và API-SC02-02 (200). {@code warnings} mang các mã lỗi
 * mức "Cảnh báo" (ERR-SC02-13/14, BR-SC02-02/03) — không chặn lưu, chỉ có tác dụng chặn hành
 * động "Trình kiểm soát" sau này (POST /cases/{id}/workflow-action, ngoài phạm vi task này).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentCaseResponse {
    String caseId;
    String caseCode;
    String statusId;
    Timestamp createdAt;
    Timestamp updatedAt;
    Map<String, String> warnings;
}
