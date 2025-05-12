package com.agribank.qldv_api.enums;

import lombok.Getter;

@Getter
public enum ERole {
    TELLER(1, "QLDV_TELLER"),
    APPROVER(2, "QLDV_APPROVER");

    private final int id;
    private final String name;

    ERole(int id, String name) {
        this.id = id;
        this.name = name;
    }
}