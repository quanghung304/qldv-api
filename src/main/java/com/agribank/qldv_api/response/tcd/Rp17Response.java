package com.agribank.qldv_api.response.tcd;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor()
@NoArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rp17Response {
    String organizationCode;
    String fullName;
    //Chức danh chuyên môn
    String mainJob;
    String ethnic;
    String religion;
    Date admissionDate;
    //ngày vào Đảng chính thức
    Date officialRecognitionDay;
    //Tuổi Đảng
    Integer partyAge;
    //Chức danh hiện tại
    String currentJob;
    //Chwucs danh quy hoạch
    String planningJob;
    //Trình độ
    String degree;
    //Học vấn
    String education;
    //Thường binh Loại
    Integer disabledType;
    //ngày nhập ngũ
    Date enlistmentDate;
    //ngày xuất ngũ
    Date dischargeDate;
    //gia đình liệt sĩ
    String martyrsFamily;
    //Có công với cách mạng
    String revolution;
    //Bản thân có làm việc trong chế độ cũ
    String oldRegime;
    //Xuất thân là công nhân
    String formerWorker;
}
