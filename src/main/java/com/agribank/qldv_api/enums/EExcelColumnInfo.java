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
    BC_01_BCSL("Thống kê số lượng Chi, Đảng bộ", "THỐNG KÊ SỐ LƯỢNG CHI, ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId(), "A4N"),
    BC_03_04_DS("Danh sách chi, Đảng bộ", "DANH SÁCH CHI ĐẢNG BỘ", ERequestType.TO_CHUC_DANG.getId(), "A4"),
    BC_07_DSDV("Thống kê Đảng viên theo thành phần gia đình", "THỐNG KÊ ĐẢNG VIÊN THEO THÀNH PHẦN GIA ĐÌNH", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_17_DSDV("Thông tin Đảng viên", "THÔNG TIN ĐẢNG VIÊN", ERequestType.TO_CHUC_DANG.getId(), "A4N"),
    BC_21_DSDV("Danh sách Đảng viên dự bị danh sách Đảng viên đến hạn công nhận Đảng viên chính thức",
            "DANH SÁCH ĐẢNG VIÊN DỰ BỊ/\nDANH SÁCH ĐẢNG VIÊN ĐẾN HẠN CÔNG NHẬN ĐẢNG VIÊN CHÍNH THỨC", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_22_DSDV("Danh sách kết nạp Đảng", "DANH SÁCH KẾT NẠP ĐẢNG", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_23_DSDV("Danh sách kết nạp Đảng lần 2", "DANH SÁCH KẾT NẠP ĐẢNG LẦN 2", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_24_DSDV("Danh sách công nhận Đảng viên chính thức", "DANH SÁCH CÔNG NHẬN ĐẢNG VIÊN CHÍNH THỨC", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_25_DSDV("Danh sách Đảng viên được khôi phục Đảng tịch", "DANH SÁCH ĐẢNG VIÊN ĐƯỢC KHÔI PHỤC ĐẢNG TỊCH", ERequestType.DANG_VIEN.getId(),"A4N"),
    BC_28_DSDV("Danh sách Đảng viên chuyển sinh hoạt Đảng đến Đảng bộ Agribank", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT ĐẢNG ĐẾN ĐẢNG BỘ AGRIBANK", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_29_DSDV("Danh sách Đảng viên chuyển sinh hoạt ra ngoài Đảng bộ Agribank", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT RA NGOÀI ĐẢNG BỘ AGRIBANK", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_30_DSDV("Danh sách Đảng viên chuyển sinh hoạt trong Đảng bộ Agribank", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT TRONG ĐẢNG BỘ AGRIBANK", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_31_DSDV("DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT TRONG ĐẢNG BỘ CƠ SỞ", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT TRONG ĐẢNG BỘ CƠ SỞ", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_33_DSDV("Danh sách đảng viên chuyển sinh hoạt đảng tạm thời", "DANH SÁCH ĐẢNG VIÊN CHUYỂN SINH HOẠT ĐẢNG TẠM THỜI", ERequestType.DANG_VIEN.getId(), "A4N"),
    BC_34_DSDV("Danh sách Đảng viên miễn sinh hoạt Đảng Xin ra khỏi Đảng Xóa tên Từ trần", "DANH SÁCH ĐẢNG VIÊN MIỄN SINH HOẠT ĐẢNG XIN RA KHỎI ĐẢNG XÓA TÊN TỪ TRẦN", ERequestType.DANG_VIEN.getId(), "A4N");
    String name;
    String nameUpperCase;
    Integer type;
    String pageType;

    public static EExcelColumnInfo getType(String name) {
        return EExcelColumnInfo.valueOf(name);
    }
}
