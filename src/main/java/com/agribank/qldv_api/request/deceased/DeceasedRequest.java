package com.agribank.qldv_api.request.deceased;

import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeceasedRequest {
    String id;
    String organizationCode;
    String staffCode;
    String decisionNumber;
    Date decisionDate;
    Date dateOfDeath;

    public void validate() {
        if (Objects.isNull(organizationCode)) {
            throw new CommonException("Organization code is required");
        }
        if (Objects.isNull(decisionNumber)) {
            throw new CommonException("Decision number is required");
        }
        if (Objects.isNull(decisionDate)) {
            throw new CommonException("Decision date is required");
        }
        if (Objects.isNull(dateOfDeath)) {
            throw new CommonException("Date of death is required");
        }


    }
}
