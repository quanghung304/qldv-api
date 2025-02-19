package com.agribank.qldv_api.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTCDResponse {
    String maSo;
    String maSoTCD;
    String ten;
    String quyen;
    String chucVu;
    String matKhau;
    String ngayMatKhau;
    String maSoThamChieu;
    String tel;
    String email;
    String trangThai;
    Date ngayTao;
    Date ngaySua;
}
