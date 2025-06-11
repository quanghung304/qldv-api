package com.agribank.qldv_api.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColumnValidate {
    Integer maxLength;
    Integer minValue;
    Integer maxValue;
    String fromDate;
    String toDate;
}
