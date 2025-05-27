package com.agribank.qldv_api.response;

import com.agribank.qldvutils.exception.CommonException;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.util.Objects;

@Data
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFormDto {
    String decisionCommittee;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;

    public void validate(){
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
