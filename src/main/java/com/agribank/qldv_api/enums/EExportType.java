package com.agribank.qldv_api.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum EExportType {
    EXCEL(1, "excel"),
    PDF(2, "pdf");

    private final int id;
    private final String type;

    EExportType(int id, String type) {
        this.id = id;
        this.type = type;
    }
}
