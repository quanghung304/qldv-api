package com.agribank.qldv_api.enums;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EApiLogType {
    INSERT("Thêm mới",0),
    UPDATE("Cập nhật", 1),
    DELETE("Xóa", 2);

    String value;
    int id;
}
