package com.agribank.qldv_api.request.establishmentDissolve;

import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveRequest extends BaseFormDto {
    String id;
    String code;
    String name;
    String form;
    Integer type;

    public void validate(){
        if (Objects.isNull(code)) {
            throw new CommonException("Code is required");
        }

        if (Objects.isNull(type)) {
            throw new CommonException("Type is required");
        }

        if (Objects.isNull(getConclusionNumber())) {
            throw new CommonException("Resolution number is required");
        }

        if (Objects.isNull(getConclusionDate())) {
            throw new CommonException("Resolution date is required");
        }

        if (Objects.isNull(getDecisionNumber())) {
            throw new CommonException("establishmentDecisionNumber date is required");
        }

        if (Objects.isNull(getEffectiveDate())) {
            throw new CommonException("effectiveDate is required");
        }
    }
}
