package com.agribank.qldv_api.request.dv;

import com.agribank.qldv_api.exception.ValidationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVDto {
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
    @Pattern(regexp = "^[MF]$", message = "Giá trị gender phải là M hoặc F")
    String gender;
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
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String martyrsFamily;
    //Có công với cachs mạng
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String revolution;
    //Thành phần xã hội khi vào Đảng
    String socialComposition;
    //công việc chính đang làm
    String mainJob;
    //Ngày kết nạp Đảng
    Date admissionDate;
    //nguồn kết nạp
    String sourceRecruitment;
    //Kết nạp tại chi bộ
    String branchPartyCode;
    //công đoàn giới thiệu
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String suggestionUnion;
    //Đoàn thanh niên giới thiệu
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
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
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String politicalIssue;
    //Chế độ cũ
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String oldRegime;
    //Xuất thân là công nhân
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String formerWorker;
    //Kết hôn với người nước ngoài
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String foreignMarriage;
    @Pattern(regexp = "^[YN]$", message = "Giá trị phải là Y hoặc N")
    String foreignRelated;
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
