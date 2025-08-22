package com.agribank.qldv_api.response.party_transfer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferWithinAgribankResponse {
    String id;
    String staffCode;
    String fullName;
    String processId;
    String decisionNumber;
    Date decisionDate;
    String decisionIssuingUnit;
    Date effectiveDate;
    Date dateOfProposal;
    String numberOfDoc;
    Date committeeProposalDate;
    String numberOfSubmission;
    String secondIntroNumber;
    Date departureReceptionDate;
    Date transferDate;
    String receivingOrgCCode;
    String receivingOrgCName;
    String receivingOrgBCode;
    String receivingOrgBName;
}
