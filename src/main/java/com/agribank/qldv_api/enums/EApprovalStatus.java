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
public enum EApprovalStatus {
    PENDING(0),
    APPROVED(1),
    DENIED(2);


    int id;

    public static int getValue(Integer value) {
        for (EApprovalStatus e : EApprovalStatus.values()) {
            if (e.getId() == value) {
                return e.id;
            }
        }
        return -1;
    }
}
