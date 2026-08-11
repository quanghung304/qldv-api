package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Map;

/** Response chung cho API-SC08-01 (201) và API-SC08-02 (200) — cùng khuôn dạng {@code EstablishmentCaseResponse}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseChangeResponse {
    String caseId;
    String caseCode;
    String statusId;
    Timestamp createdAt;
    Timestamp updatedAt;
    Map<String, String> warnings;
}
