package com.agribank.qldv_api.request.membershipProposalDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MPUpdateRequest{
    String id;
    String reason;
    //Số kết luận nghị quyết
    String resolutionNumber;
    //Ngày kết luận nghị quyết
    Date resolutionDate;
    //Số quyết định
    String decisionNumber;
    //Ngày QĐ
    Date decisionDate;

    public void validate() {
        if (Objects.isNull(id)) {
            throw new CommonException("id is required");
        }
    }
}
