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
    BIEU_12("B12", "KẾ HOẠCH PHÁT TRIỂN ĐẢNG VIÊN", ERequestType.DANG_VIEN.getId()),
    BIEU_15("B15", "THÔNG TIN CHUNG VỀ QUẦN CHÚNG/ĐẢNG VIÊN", ERequestType.DANG_VIEN.getId()),
    BIEU_20("B20", "Đề nghị kết nạp Đảng", ERequestType.DANG_VIEN.getId()),
    BIEU_26_PARTY_ACTIVITY_EXEMPTION("BIEU_26_EXEM", "MIỄN SINH HOẠT ĐẢNG", ERequestType.DANG_VIEN.getId()),
    BIEU_26_LEAVE_PARTY("BIEU_26_LEAV", "RA KHỎI ĐẢNG", ERequestType.DANG_VIEN.getId()),
    BIEU_26_REMOVE_NAME_PARTY("BIEU_26_RMV", "XÓA TÊN", ERequestType.DANG_VIEN.getId()),
    BIEU_26_DECEASED("BIEU_26_DECE", "TỪ TRẦN", ERequestType.DANG_VIEN.getId());
    String code;
    String name;
    Integer type;
}
