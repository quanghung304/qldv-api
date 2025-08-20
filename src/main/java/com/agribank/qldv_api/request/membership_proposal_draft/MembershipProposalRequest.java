package com.agribank.qldv_api.request.membership_proposal_draft;

import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  MembershipProposalRequest {
    String id;
    @NotBlank(message = "Họ tên quần chúng không được phép bỏ trống")
    @NotNull(message = "Họ tên quần chúng không được phép bỏ trống")
    String fullName;
    @NotBlank(message = "Mã cán bộ không được phép bỏ trống")
    @NotNull(message = "Mã cán bộ không được phép bỏ trống")
    String staffCode;
    @NotBlank(message = "Chưa chọn Tổ chức Đảng")
    @NotNull(message = "Chưa chọn Tổ chức Đảng")
    String organizationCode;
    String reason;
    //Số kết luận nghị quyết
    String resolutionNumber;
    //Ngày kết luận nghị quyết
    Date resolutionDate;
    //Số quyết định
    String decisionNumber;
    //Ngày QĐ
    Date decisionDate;

    public void validate(){
        if (Objects.isNull(reason) || reason.isBlank()){
            return;
        }

        if (Objects.isNull(resolutionNumber) || resolutionNumber.isBlank()){
            throw new CommonException("Số KL/NQ không được để trống");
        }

        if (Objects.isNull(resolutionDate)){
            throw new CommonException("Ngày ban hành KL/NQ không được để trống");
        }

        if (Objects.isNull(decisionNumber) || decisionNumber.isBlank()){
            throw new CommonException("Số QĐ không được để trống");
        }

        if (Objects.isNull(decisionDate)){
            throw new CommonException("Ngày QĐ không được để trống");
        }

        Date currentDate = CommonUtils.getCurrentDate();
        if (CommonUtils.validateDatesAfter(currentDate, resolutionDate)){
            throw new CommonException("Ngày ban hành KL/NQ không được chọn ngày tương lai");
        }

        if (CommonUtils.validateDatesAfter(currentDate, decisionDate)){
            throw new CommonException("Ngày QĐ không được chọn ngày tương lai");
        }
    }
}
