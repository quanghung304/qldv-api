package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/** 1 tổ chức đảng vừa được tạo bởi API-SC06-02 (Thành lập/Sáp nhập/Hợp nhất=1, Chia tách=N). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompletedOrganizationResponse {
    String organizationId;
    String organizationCode;
}
