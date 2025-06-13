package com.agribank.qldv_api.response.transformation_history;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransformationHistoryResponse {
    String oldForm;
    String newForm;
    Date effectiveDate;
}
