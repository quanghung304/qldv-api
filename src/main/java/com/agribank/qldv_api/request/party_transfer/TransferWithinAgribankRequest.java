package com.agribank.qldv_api.request.party_transfer;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferWithinAgribankRequest {
    @NotNull(message = "Không được để trống trường mã cán bộ")
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
}
