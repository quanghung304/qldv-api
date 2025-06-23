package com.agribank.qldv_api.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ETransferType {
    TRANSFER_TO_AGRIBANK(1, "Chuyển đến Đảng bộ Agribank"),
    TRANSFER_OUT_AGRIBANK(2, "Chuyển ra ngoài Đảng bộ Agribank"),
    TRANSFER_WITHIN_AGRIBANK(3, "Chuyển trong Đảng bộ Agribank"),
    TRANSFER_WITHIN_BASE(4, "Chuyển trong ĐBCS"),
    TEMPORARY_TRANSFER(5, "Chuyển sinh hoạt đảng tạm thờI");

    private final int id;
    private final String name;

    ETransferType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ETransferType getTransferType(int i) {
        for (ETransferType type: ETransferType.values()) {
            if (Objects.equals(i, type.id)) {
                return type;
            }
        }

        return null;
    }
}
