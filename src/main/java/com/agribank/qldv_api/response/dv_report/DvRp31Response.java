package com.agribank.qldv_api.response.dv_report;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DvRp31Response {
    String id;
    String organizationCodeB; //Mã chi đảng bộ
    String oldOrganizationNameC; // Mã đảng bộ nơi đi
    String oldOrganizationNameD; // Tên đảng bộ nơi đi
    String staffCode;
    String fullName;
    Date birthday;
    // Vị trí/ Chức danh chuyên môn
    // Đơn vị công tác
    // Chức danh cấp ủy
    String organizationNameC;
    String organizationNameD;
    Date effectiveDate; // Ngày chuyển công tác
    Date transferDate; // Ngày chuyển SHĐ

    String implementationStaff; //cán bộ thực hiện
    String controller; //kiểm soát viên
    //String boardLeader; //Lãnh đạo ban

}
