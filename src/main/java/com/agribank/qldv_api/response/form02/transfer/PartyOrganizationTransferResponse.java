package com.agribank.qldv_api.response.form02.transfer;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyOrganizationTransferResponse {
    String id;
    String docNumber;
    Date docDate;
    String receivingOrgName;
    String receivingOrgCode;
    String decisionCommittee;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
}
