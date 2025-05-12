package com.agribank.qldv_api.request.establishmentDissolve;

import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveRequest {
    String id;
    String code;
    String name;
    String form;
    Integer type;
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

    public void validate(){
        if (Objects.isNull(code)) {
            throw new CommonException("Code is required");
        }

        if (Objects.isNull(type)) {
            throw new CommonException("Type is required");
        }

        if (Objects.isNull(resolutionNumber)) {
            throw new CommonException("Resolution number is required");
        }

        if (Objects.isNull(resolutionDate)) {
            throw new CommonException("Resolution date is required");
        }

        if (Objects.isNull(establishmentDecisionNumber)) {
            throw new CommonException("establishmentDecisionNumber date is required");
        }

        if (Objects.isNull(effectiveDate)) {
            throw new CommonException("effectiveDate is required");
        }
    }
}
