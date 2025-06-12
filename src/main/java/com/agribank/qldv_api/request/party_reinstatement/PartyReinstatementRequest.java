package com.agribank.qldv_api.request.party_reinstatement;

import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyReinstatementRequest {
    String id;
    String organizationCode;
    String staffCode;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;

    public void validate() {
        if (Objects.isNull(organizationCode) || organizationCode.isBlank()) {
            throw new CommonException("Cấp ủy quyết định không được bỏ trống");
        }

        if (Objects.isNull(staffCode) || staffCode.isBlank()){
            throw new CommonException("Họ và tên không được bỏ trống");
        }

        if (Objects.isNull(conclusionNumber) || conclusionNumber.isBlank()){
            throw new CommonException("Số KL/Nghị quyết không được bỏ trống");
        }

        if (Objects.isNull(conclusionDate)){
            throw new CommonException("Ngày KL/Nghị quyết không được bỏ trống");
        }

        if (Objects.isNull(decisionNumber) || decisionNumber.isBlank()){
            throw new CommonException("Số QĐ không được bỏ trống");
        }

        if (Objects.isNull(decisionDate)){
            throw new CommonException("Ngày QĐ không được bỏ trống");
        }
    }
}
