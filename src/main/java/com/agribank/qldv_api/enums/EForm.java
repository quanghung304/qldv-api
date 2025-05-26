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
    BIEU_01("B01", "THÔNG TIN TÊN, HÌNH THỨC CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BIEU_02_ESTA("B02_ESTA", "THÀNH LẬP, GIẢI THỂ CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BIEU_02_HIST("B02_HIST", "NÂNG CẤP, HẠ CẤP CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId());;

    String code;
    String name;
    Integer type;
}
