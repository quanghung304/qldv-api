package com.agribank.qldv_api.response.excel_column_info;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelColumnInfoResponse {
    String id;
    String code;
    String columnField;
    String columnName;
    String columnTitle;
    String columnType;
    Integer sortOrder;
    int width;
    Integer isShow;
    String align;
}
