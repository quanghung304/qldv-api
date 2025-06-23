package com.agribank.qldv_api.request.excel_column_info;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelColumnInfoRequest {
    String id;
    Integer isShow;
}
