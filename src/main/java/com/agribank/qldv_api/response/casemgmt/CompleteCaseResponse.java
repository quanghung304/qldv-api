package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Response của API-SC06-02 (POST /cases/{id}/complete) — dùng chung cho cả 6 loại nghiệp vụ.
 * {@code organizations}: tổ chức vừa tạo (Thành lập/Sáp nhập/Hợp nhất=1, Chia tách=N, rỗng với
 * Giải thể/Đổi tên). {@code affectedOrganizationIds}: TOÀN BỘ tổ chức bị đổi trạng thái/tên (kể
 * cả tổ chức con bị kéo theo khi cascade giải thể).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteCaseResponse {
    String caseId;
    String statusId;
    List<CompletedOrganizationResponse> organizations;
    List<String> affectedOrganizationIds;
}
