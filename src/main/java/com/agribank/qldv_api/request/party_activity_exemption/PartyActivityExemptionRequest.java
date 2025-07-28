package com.agribank.qldv_api.request.party_activity_exemption;

import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyActivityExemptionRequest {
    String id;
    String organizationCode;
    String staffCode;
    String committeeDecision;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
    String reason;

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

        if (Objects.isNull(decisionNumber) || decisionNumber.isBlank()) {
            throw new CommonException("Chưa nhập số QĐ");
        }

        if (Objects.isNull(decisionDate)) {
            throw new CommonException("Chưa chọn ngày ban hành QĐ");
        }

        if (Objects.isNull(effectiveDate)) {
            throw new CommonException("Chưa chọn ngày hiệu lực");
        }

        if (Objects.isNull(reason) || reason.isBlank()) {
            throw new CommonException("Chưa nhập lý do miễn sinh hoạt Đảng");
        }

        if (!CommonUtils.validateDatesAfter(decisionDate, effectiveDate)){
            throw new CommonException("Ngày hiệu lực phải sau ngày quyết đinh");
        }

    }
}
