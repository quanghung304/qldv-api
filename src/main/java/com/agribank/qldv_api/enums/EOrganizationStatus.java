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
public enum EOrganizationStatus {
    YES("Y"),
    NO("N");

    String status;

    public static String getValue(String value) {
        for (EOrganizationStatus e : EOrganizationStatus.values()) {
            if (e.getStatus().equals(value)) {
                return e.getStatus();
            }
        }
        return null;
    }
}
