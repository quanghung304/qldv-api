package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum EExcelColumnInfo {
    BC_03_04_DS("Danh sách chi, Đảng bộ", "DANH SÁCH CHI ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId()),
    BC_17_DSDV("Thông tin Đảng viên", "THÔNG TIN ĐẢNG VIÊN", ERequestType.TO_CHUC_DANG.getId()),
    BC_21_DSDV("Danh sách Đảng viên dự bị danh sách Đảng viên đến hạn công nhận Đảng viên chính thức",
            "DANH SÁCH ĐẢNG VIÊN DỰ BỊ/\nDANH SÁCH ĐẢNG VIÊN ĐẾN HẠN CÔNG NHẬN ĐẢNG VIÊN CHÍNH THỨC", ERequestType.DANG_VIEN.getId()),
    BC_24_DSDV("Danh sách công nhận Đảng viên chính thức", "DANH SÁCH CÔNG NHẬN ĐẢNG VIÊN CHÍNH THỨC", ERequestType.DANG_VIEN.getId()),
    BC_25_DSDV("Danh sách Đảng viên được khôi phục Đảng tịch", "DANH SÁCH ĐẢNG VIÊN ĐƯỢC KHÔI PHỤC ĐẢNG TỊCH", ERequestType.DANG_VIEN.getId()),
    BC_29_DSDV("Danh sách Đảng viên chuyển sinh hoạt ra ngoài Đảng bộ Agribank", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT RA NGOÀI ĐẢNG BỘ AGRIBANK", ERequestType.DANG_VIEN.getId()),
    BC_34_DSDV("Danh sách Đảng viên miễn sinh hoạt Đảng Xin ra khỏi Đảng Xóa tên Từ trần",
            "DANH SÁCH ĐẢNG VIÊN MIỄN SINH HOẠT ĐẢNG XIN RA KHỎI ĐẢNG XÓA TÊN TỪ TRẦN",
            ERequestType.DANG_VIEN.getId());

    String name;
    String nameUpperCase;
    Integer type;

    public static EExcelColumnInfo getType(String name) {
        return EExcelColumnInfo.valueOf(name);
    }
}
