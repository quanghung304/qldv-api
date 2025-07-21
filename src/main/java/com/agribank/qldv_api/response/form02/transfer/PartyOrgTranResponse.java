package com.agribank.qldv_api.response.form02.transfer;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyOrgTranResponse {
    String id;
    String receivingOrgCode;
    String receivingOrgName;
    String decisionCommittee;
    String decisionCommitteeName;
    //("cap quyet dinh")
    //("so ket luan/nghi quyet")
    String conclusionNumber;
    //("ngay ket luan/nghi quyet")
    Date conclusionDate;
    //("so quyet dinh")
    String decisionNumber;
    //("ngay quyet dinh")
    Date decisionDate;
    //("ngay hieu luc")
    Date effectiveDate;
    List<PartyOrgTranDetailResponse> partyOrgTranDetails;
}
