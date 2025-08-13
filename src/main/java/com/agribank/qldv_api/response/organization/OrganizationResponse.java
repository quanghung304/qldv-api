package com.agribank.qldv_api.response.organization;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationResponse {
    String id;
    String code;
    String name;
    Integer brcd;
    //hình thức
    String form;
    //Mã tcd cấp trên
    String parentCode;
    //được ủy quyền kết nạp, khai trừ
    Integer authorized;
    //Số kết luận/nghị quyết
    String resolutionNumber;
    //Ngày kết luận/nghị quyết
    Date resolutionDate;
    //Số quyết định thành lập
    String establishmentDecisionNumber;
    //Ngày quyết định
    Date decisionDate;
    //Ngày hiệu lực
    Date effectiveDate;
    //trạng thái đang hoạt động, giải thể
    String status;
    Timestamp createdAt;
    Timestamp updatedAt;
}
