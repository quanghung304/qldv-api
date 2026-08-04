package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Response của API-GL-01 (DELETE /cases/{id}) — theo đúng envelope chuẩn của toàn tài liệu. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseDeleteResponse {
    String caseId;
    Boolean deleted;
}
