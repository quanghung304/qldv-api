package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EReport01Type {
    ESTABLISH(0, "Thành lập"),
    UPGRADE(1, "Nâng cấp"),
    DOWNGRADE(2, "Hạ cấp"),
    DECOMPOSE(3, "Chia tách"),
    MERGE(4, "Sáp nhập"),
    UNION(5, "Hợp nhất"),
    DISSOLVE(6, "Giải thể"),
    DISBAND(7, "Giải tán"),
    TRANSFER(8, "Chuyển giao"),
    RENAME(9, "Đổi tên");

    int id;
    String name;

    public static int getValue(Integer value) {
        for (EReport01Type e : EReport01Type.values()) {
            if (e.getId() == value) {
                return e.id;
            }
        }
        return -1;
    }
}
