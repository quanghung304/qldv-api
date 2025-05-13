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
    ESTABLISH(0),
    UPGRADE(1),
    DOWNGRADE(2),
    DECOMPOSE(3),
    MERGE(4),
    UNION(5),
    DISSOLVE(6),
    DISBAND(7);

    int id;

    public static int getValue(Integer value) {
        for (EReport01Type e : EReport01Type.values()) {
            if (e.getId() == value) {
                return e.id;
            }
        }
        return -1;
    }
}
