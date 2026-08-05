package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldv_api.response.doctemplate.GenerateDocumentsResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Response của API-SC06-01 (POST /cases/{id}/archive). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArchiveCaseResponse {
    String caseId;
    String statusId;
    GenerateDocumentsResponse documents;
}
