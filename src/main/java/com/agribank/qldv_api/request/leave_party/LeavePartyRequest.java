package com.agribank.qldv_api.request.leave_party;

import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeavePartyRequest {
    String id;
    String organizationCode;
    String staffCode;
    String committeeDecision;
    String resolutionNumber;
    Date resolutionDate;
    String reason;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;

    public void validate() {
        if (Objects.isNull(organizationCode) || organizationCode.isBlank()) {
            throw new CommonException("Chưa chọn Tổ chức Đảng");
        }
        if (Objects.isNull(staffCode) || staffCode.isBlank()) {
            throw new CommonException("Kiểm tra lại Đảng viên");
        }
        if (Objects.isNull(committeeDecision) || committeeDecision.isBlank()) {
            throw new CommonException("Chưa chọn cấp ủy quyết định");
        }
        if (Objects.isNull(resolutionNumber) || resolutionNumber.isBlank()) {
            throw new CommonException("Chưa nhập Số KL/NQ");
        }
        if (Objects.isNull(resolutionDate)) {
            throw new CommonException("Chưa chọn Ngày ban hành KL/NQ");
        }
        if (Objects.isNull(reason) || reason.isBlank()) {
            throw new CommonException("Chưa nhập lý do");
        }
        if (Objects.isNull(decisionNumber)) {
            throw new CommonException("Chưa nhập Số QĐ");
        }
        if (Objects.isNull(decisionDate)) {
            throw new CommonException("Chưa chọn ngày ban hành QĐ");
        }
        if (Objects.isNull(effectiveDate)) {
            throw new CommonException("Chưa chọn Ngày hiệu lực");
        }

        if (!CommonUtils.validateDatesAfter(decisionDate, effectiveDate)){
            throw new CommonException("Ngày hiệu lực phải sau ngày quyết đinh");
        }
    }
}
