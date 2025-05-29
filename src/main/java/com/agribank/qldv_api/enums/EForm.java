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
    BIEU_02_ESTA("B02_ESTA", "GIẢI THỂ CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BIEU_02_UP("B02_UP", "NÂNG CẤP CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BIEU_02_DOWN("B02_DOWN", "HẠ CẤP CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BIEU_15("B15", "THÔNG TIN CHUNG VỀ QUẦN CHÚNG/ĐẢNG VIÊN", ERequestType.DANG_VIEN.getId());

    String code;
    String name;
    Integer type;
}
