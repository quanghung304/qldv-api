package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldvutils.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GC-S3-02-04: {@code missing} rỗng nghĩa là đủ điều kiện và hồ sơ ĐÃ chuyển A-15 → A-16
 * ({@code statusId} phản ánh trạng thái SAU cùng). {@code missing} không rỗng nghĩa là dữ liệu
 * gửi lên đã lưu thành công (response vẫn 200) nhưng CHƯA đủ để tự động chuyển bước — liệt kê
 * đúng phần còn thiếu cho từng bộ văn bản (tinh thần ERR-SC05-03, không phải lỗi chặn lưu).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDocumentsResponse {
    String caseId;
    String statusId;
    List<Document> documents;
    Map<String, String> missing;
    Map<String, String> warnings;
}
