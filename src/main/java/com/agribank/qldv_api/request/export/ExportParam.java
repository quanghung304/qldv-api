package com.agribank.qldv_api.request.export;

import com.agribank.qldvutils.entity.ExcelColumnInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportParam {
    String fileType;
    List<ExcelColumnInfo> columnsExport;
    String title;
    Object serviceParameter;
    String period;
    String pageSize;
    Integer numberTableHeaderRows;
}
