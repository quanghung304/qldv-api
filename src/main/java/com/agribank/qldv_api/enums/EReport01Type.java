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
    UPGRADE(0),
    DOWNGRADE(1),
    DECOMPOSE(2),
    MERGE(3),
    UNION(4),
    DISSOLVE(5),
    DISBAND(6);

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
