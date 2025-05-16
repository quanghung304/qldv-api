package com.agribank.qldv_api.request.membershipProposalDraft;

import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipProposalRequest {
    String staffCode;
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
        if (Objects.isNull(staffCode)) {
            throw new ValidationException("staffCode code is required");
        }

        if (Objects.isNull(resolutionNumber)) {
            throw new CommonException("Resolution number is required");
        }

        if (Objects.isNull(resolutionDate)) {
            throw new CommonException("resolutionDate is required");
        }

        if (Objects.isNull(decisionNumber)) {
            throw new CommonException("decisionNumber  is required");
        }

        if (Objects.isNull(decisionDate)) {
            throw new CommonException("decisionDate is required");
        }
    }
}
