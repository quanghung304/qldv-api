package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EExcelImport {
    USERS( "Import User"),
    BIEU_1("Thành lập Tổ chức Đảng"),
    BIEU_12( "Phát triển Đảng viên"),
    BIEU_15("Import Đảng viên");


    final String value;

    EExcelImport(String value) {
        this.value = value;
    }
}
