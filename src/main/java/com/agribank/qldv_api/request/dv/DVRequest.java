package com.agribank.qldv_api.request.dv;

import com.agribank.qldv_api.exception.ValidationException;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVRequest {
    String id;
    //staff code
    String staffCode;
    @NotNull
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
    Integer gender;
    //họ tên đang sử dụng
    String usingName;
    Timestamp birthday;
    String hometown;
    String birthPlace;
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
    Integer martyrsFamily;
    //Có công với cachs mạng
    Integer revolution;
    //Thành phần xã hội khi vào Đảng
    String socialComposition;
    //công việc chính đang làm
    String mainJob;
    //Ngày kết nạp Đảng
    Date admissionDate;
    //nguồn kết nạp
    Integer sourceRecruitment;
    //Kết nạp tại chi bộ
    String branchPartyCode;
    //công đoàn giới thiệu
    Integer suggestionUnion;
    //Đoàn thanh niên giới thiệu
    Integer suggestionYouthUnion;
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
    Integer politicalIssue;
    //Chế độ cũ
    Integer oldRegime;
    //Xuất thân là công nhân
    Integer formerWorker;
    //Kết hôn với người nước ngoài
    Integer foreignMarriage;
    //Trình độ
    String degree;
    //Học vấn phổ thông: 10/10, 12/12, khác
    String education;
    //Tình trạng sức khỏe bản thân: Tốt/ bình thường/ khác
    String healthCondition;
    //Ngày, tháng, năm từ trần
    String dateOfDeath;
    Integer status;

    public void validate(){
        if (Objects.isNull(staffCode)){
            throw new ValidationException("Code is required");
        }

        if (Objects.isNull(organizationCode)){
            throw new ValidationException("OrganizationCode is required");
        }

        if (Objects.isNull(birthday)){
            throw new ValidationException("Birthday is required");
        }

        if (Objects.isNull(hometown)){
            throw new ValidationException("Hometown is required");
        }

        if (Objects.isNull(birthPlace)){
            throw new ValidationException("BirthPlace is required");
        }

        if (Objects.isNull(permanentResidence)){
            throw new ValidationException("PermanentResidence is required");
        }

        if (Objects.isNull(temporaryResidence)){
            throw new ValidationException("TemporaryResidence is required");
        }

        if (Objects.isNull(religion)){
            throw new ValidationException("Religion is required");
        }

        if (Objects.isNull(ethnic)){
            throw new ValidationException("Ethnic is required");
        }

        if (Objects.isNull(admissionDate)){
            throw new ValidationException("AdmissionDate is required");
        }
    }
}
