package com.agribank.qldv_api.request.membershipProposalDraft;

import com.agribank.qldv_api.exception.ValidationException;
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
public class MembershipProposalRequest {
    String id;
    @NotBlank(message = "Full name not blank")
    @NotNull(message = "fullName is required")
    String fullName;
    @NotBlank(message = "staffCode code not blank")
    @NotNull(message = "staffCode code is required")
    String staffCode;
    @NotBlank(message = "Organization Code not blank")
    @NotNull(message = "Organization Code is required")
    String organizationCode;
    String reason;
    //Số kết luận nghị quyết
    @NotBlank(message = "Resolution Number not blank")
    @NotNull(message = "Resolution Number is required")
    String resolutionNumber;
    //Ngày kết luận nghị quyết
    @NotNull(message = "Resolution Date is required")
    Date resolutionDate;
    //Số quyết định
    @NotBlank(message = "Decision Number not blank")
    @NotNull(message = "Decision Number is required")
    String decisionNumber;
    //Ngày QĐ
    @NotNull(message = "Decision Date is required")
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

        if (Objects.isNull(organizationCode)) {
            throw new CommonException("organizationCode is required");
        }

        if (Objects.isNull(fullName)) {
            throw new CommonException("fullName is required");
        }
    }
}
