package com.agribank.qldv_api.response.dv;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVResponse {
    String id;
    String staffCode;
    String organizationCode;
    //Số lý lịch
    String resumeNumber;
    //số thẻ Đảng viên
    String partyCardNumber;
    //ngày cấp thẻ Đảng
    Date issueDate;
    //cccd
    String vneid;
    String fullName;
    String gender;
    //họ tên đang sử dụng
    String usingName;
    Timestamp birthday;
    String birthPlace;
    String hometown;
    //hộ khẩu thường trú
    String permanentResidence;
    //tạm trú
    String temporaryResidence;
    //dân tộc
    String ethnic;
    //Tôn giáo
    String religion;
    //thành phần gia đình
    String familyComposition;
    //Gia đình liệt sĩ
    String martyrsFamily;
    //Có công với cachs mạng
    String revolution;
    //Thành phần xã hội khi vào Đảng
    String socialComposition;
    //công việc chính đang làm
    String mainJob;
    //Ngày kết nạp Đảng
    Date admissionDate;
    //nguồn kết nạp
    String sourceRecruitment;
    String externalParty;
    //Kết nạp tại chi bộ
    String branchPartyCode;
    //công đoàn giới thiệu
    String suggestionUnion;
    //Đoàn thanh niên giới thiệu
    String suggestionYouthUnion;
    //Người giới thiệu 1
    String referrer1;
    //Chức vụ đơn vị của người giới thiệu;
    String jobPosition1;
    String referrer2;
    String jobPosition2;
    //Ngày công nhận chính thức
    Date officialRecognitionDay;
    //tham gia tổ chức khác
    String recruitAnotherOrganization;
    //Ngày tuyển vào Agribank
    Date agriRecruitDate;
    //Chi nhánh tuyển dụng
    String recruitBrcd;
    //Ngày vào Đoàn
    Date youthUnionJoinDate;
    //Tổ chức xã hội khác
    String otherSocialOrganization;
    //Ngày nhập ngũ
    Date enlistmentDate;
    //Ngày xuất ngũ
    Date dischargeDate;
    //Loại thương binh
    Integer disabledType;
    //Có vấn đề chính trị
    String politicalIssue;
    //Chế độ cũ
    String oldRegime;
    //Xuất thân là công nhân
    String formerWorker;
    //Kết hôn với người nước ngoài
    String foreignMarriage;
}
