package com.agribank.qldv_api.request.party_transfer;

import com.agribank.qldvutils.enums.EDecisionIssuingUnit;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferTemporaryRequest {
    String id;

    //----------------- CAP B -----------------//
    @NotNull(message = "Không được để trống trường mã cán bộ")
    String staffCode;
    String fullName;
    String decisionNumber;
    Date issueDate;
    Date effectiveDate;
    EDecisionIssuingUnit decisionIssuingUnit;
    String decisionIssuingUnitOther;
    String reason;
    Date partyCellRequestDate;
    String partyCellRequestNumber;
    Date partyCommitteeRequestDate;
    String partyCommitteeRequestNumber;

    //------------ DANG UY -------------//
    String transferReferralNumber;
    Date signTransferReferralDate;
    Date startTransferTemporaryDate;
    Date endTransferTemporaryDate;
    Date extendEndTransferTemporaryDate;
    Date receiptDate;
    String receivingOrgCode;
    String receivingOutOrgName;

}
