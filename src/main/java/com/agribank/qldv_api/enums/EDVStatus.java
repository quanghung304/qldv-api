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
public enum EDVStatus {
    COMMONER("0", "Quần chúng"),
    PARTY_MEMBER("1", "Đảng viên"),
    WAITING_FOR_PARTY_ACTIVITIES_TRANSFER("2", "Chờ chuyển sinh hoạt Đảng"),
    TEMPORARY_PARTY_ACTIVITIES("3", "Sinh hoạt Đảng tạm thời"),
    OUTSIDE_AGRIBANK("4", "Chuyển sinh hoạt Đảng ra ngoài Agribank"),
    PARTY_ACTIVITY_EXEMPTION("5", "Miễn sinh hoạt Đảng"),
    LEAVE_PARTY("6", "Ra khỏi Đảng"),
    REMOVE_NAME_PARTY("7", "Xóa tên"),
    DECEASED("8", "Từ trần"),
    PARTY_REINSTATEMENT("9", "Khôi phục Đảng tịch");

    String status;
    String name;
}
