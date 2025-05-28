package com.agribank.qldv_api.request.leaveParty;

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
        if (Objects.isNull(organizationCode)) {
            throw new CommonException("Organization code is required");
        }
        if (Objects.isNull(staffCode)) {
            throw new CommonException("Staff code is required");
        }
        if (Objects.isNull(committeeDecision)) {
            throw new CommonException("Committee decision is required");
        }
        if (Objects.isNull(resolutionNumber)) {
            throw new CommonException("Resolution number is required");
        }
        if (Objects.isNull(resolutionDate)) {
            throw new CommonException("Resolution date is required");
        }
        if (Objects.isNull(reason)) {
            throw new CommonException("Reason is required");
        }
        if (Objects.isNull(decisionNumber)) {
            throw new CommonException("Decision number is required");
        }
        if (Objects.isNull(decisionDate)) {
            throw new CommonException("Decision date is required");
        }
        if (Objects.isNull(effectiveDate)) {
            throw new CommonException("Effective date is required");
        }

        if (!CommonUtils.validateDatesAfter(decisionDate, effectiveDate)){
            throw new CommonException("Ngày hiệu lực phải sau ngày quyết đinh");
        }
    }
}
