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
public enum EExcelColumnInfo {
    BC_03_04_DS("Danh sách chi, Đảng bộ", "DANH SÁCH CHI ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BC_17_DSDV("Thông tin Đảng viên", "THÔNG TIN ĐẢNG VIÊN", ERequestType.TO_CHUC_DANG.getId()),
    BC_24_DSDV("Danh sách công nhận Đảng viên chính thức", "DANH SÁCH CÔNG NHẬN ĐẢNG VIÊN CHÍNH THỨC", ERequestType.DANG_VIEN.getId());

    String name;
    String nameUpperCase;
    Integer type;
}
