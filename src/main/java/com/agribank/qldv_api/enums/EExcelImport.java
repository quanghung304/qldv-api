package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EExcelImport {
    USERS( "Import User");


    final String value;

    EExcelImport(String value) {
        this.value = value;
    }
}
