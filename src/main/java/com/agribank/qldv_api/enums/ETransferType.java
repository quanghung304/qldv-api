package com.agribank.qldv_api.enums;

import lombok.Getter;

@Getter
public enum ETransferType {
    TRANSFER_TO_AGRIBANK(1),
    TRANSFER_OUT_AGRIBANK(2),
    TRANSFER_WITHIN_AGRIBANK(3),
    TRANSFER_WITHIN_BASE(4),
    TEMPORARY_TRANSFER(5);

    private final int id;

    ETransferType(int id) {
        this.id = id;
    }
}
