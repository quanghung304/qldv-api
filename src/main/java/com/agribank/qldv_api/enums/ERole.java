package com.agribank.qldv_api.enums;

import lombok.Getter;

@Getter
public enum ERole {
    XDCB_SYSTEM_ADMIN(1),
    XDCB_BRANCH_ADMIN(2),
    XDCB_MANAGER(3),
    XDCB_TELLER(4);

    private final int id;

    ERole(int id) {
        this.id = id;
    }
}