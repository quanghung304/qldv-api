package com.agribank.qldv_api.request.organization;

import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationRequest {
    String code;
    String name;
    String form;
    //được ủy quyền kết nạp, khai trừ
    Integer authorized;
    //Số kết luận/nghị quyết
    String resolutionNumber;
    //Ngày kết luận/nghị quyết
    Date resolutionDate;
    //Số quyết định thành lập
    String establishmentDecisionNumber;
    //Ngày quyết định
    Date decisionDate;
    //Ngày hiệu lực
    Date effectiveDate;
    String status;

    public void validate(){
        if (Objects.isNull(code) || code.isBlank()){
            throw new CommonException("code is null");
        }

        if (Objects.isNull(resolutionNumber) || resolutionNumber.isBlank()){
            throw new CommonException("resolutionNumber is null");
        }

        if (Objects.isNull(decisionDate)){
            throw new CommonException("decisionDate is null");
        }

        if (Objects.isNull(effectiveDate)){
            throw new CommonException("effectiveDate is null");
        }

        if (Objects.isNull(resolutionDate)){
            throw new CommonException("resolutionDate is null");
        }

        if (Objects.isNull(establishmentDecisionNumber) || establishmentDecisionNumber.isBlank()){
            throw new CommonException("establishmentDecisionNumber is null");
        }

        if (!CommonUtils.validateDatesAfter(decisionDate, effectiveDate)){
            throw new CommonException("Ngày hiệu lực phải sau ngày quyết đinh");
        }

        if (Objects.isNull(form) || form.isBlank()){
            throw new CommonException("form is null");
        }
    }
}
