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
public enum EReport26 {
    PARTY_ACTIVITY_EXEMPTION(0),
    LEAVE_PARTY(1),
    REMOVE_NAME_PARTY(2),
    DECEASED(3);

    int id;

    public static int getValue(Integer value) {
        for (EReport26 e : EReport26.values()) {
            if (e.getId() == value) {
                return e.id;
            }
        }
        return -1;
    }
}
