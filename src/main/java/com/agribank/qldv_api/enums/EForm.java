package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EForm {
    BIEU_01("B01", "THÔNG TIN TÊN, HÌNH THỨC CHI, ĐẢNG BỘ"),
    BIEU_02("B02", "THÀNH LẬP MỚI, CHUYỂN ĐỔI CHI, ĐẢNG BỘ");

    String code;
    String name;
}
