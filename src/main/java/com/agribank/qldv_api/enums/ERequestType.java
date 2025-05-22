package com.agribank.qldv_api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ERequestType {
    TO_CHUC_DANG(0),
    DANG_VIEN(1),
    CAN_BO(2);

    private final int id;
}
