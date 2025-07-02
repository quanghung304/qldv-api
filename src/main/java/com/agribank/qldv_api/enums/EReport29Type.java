package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EReport29Type {
    LATE(0),
    ON_TIME(1),
    ALL(2);

    int id;

    public static int getValue(Integer value) {
        for (EReport29Type e : EReport29Type.values()) {
            if (e.getId() == value) {
                return e.id;
            }
        }
        return -1;
    }
}
