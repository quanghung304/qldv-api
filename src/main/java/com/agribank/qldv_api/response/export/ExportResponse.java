package com.agribank.qldv_api.response.export;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportResponse {
    String fileName;
    Workbook workbook;
    File fileZip;
}
