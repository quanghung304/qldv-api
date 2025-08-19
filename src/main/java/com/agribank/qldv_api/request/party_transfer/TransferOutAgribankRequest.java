package com.agribank.qldv_api.request.party_transfer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class TransferOutAgribankRequest {
    String id;
    @NotNull(message = "Không được để trống trường mã cán bộ")
    String staffCode;
    String decisionNumber;
    Date issueDate;
    Date effectiveDate;
    String reason;
    String issuingOrganization;
    Date orgCProposeDate;
    String orgCProposeNumber;
    Date orgBProposeDate;
    String orgBProposeNumber;
    Date expectedExpiryDate;
    String introDocumentNumber;
    Date transferDate;
    String receivedOrganization;
}
