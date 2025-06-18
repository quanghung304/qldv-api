package com.agribank.qldv_api.enums;

import lombok.Getter;

@Getter
public enum EProcessStatus {
    PROCESSING(0),
    DONE(1);

    private final int id;

    EProcessStatus(int id) {
        this.id = id;
    }
}
