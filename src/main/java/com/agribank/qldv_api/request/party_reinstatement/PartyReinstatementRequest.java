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
        if (Objects.isNull(organizationCode)){
            throw new CommonException("Organization code is required");
        }

        if (Objects.isNull(staffCode)){
            throw new CommonException("Staff code is required");
        }

        if (Objects.isNull(conclusionNumber)){
            throw new CommonException("Conclusion number is required");
        }

        if (Objects.isNull(conclusionDate)){
            throw new CommonException("Conclusion date is required");
        }

        if (Objects.isNull(decisionNumber)){
            throw new CommonException("Decision number is required");
        }

        if (Objects.isNull(decisionDate)){
            throw new CommonException("Decision date is required");
        }
    }
}
