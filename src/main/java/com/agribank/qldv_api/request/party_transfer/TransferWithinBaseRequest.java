package com.agribank.qldv_api.request.party_transfer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class TransferWithinBaseRequest {
    String id;
    @NotNull(message = "Không được để trống trường mã cán bộ")
    String staffCode;
    String decisionNumber;
    Date issueDate;
    Date effectiveDate;
    String issuingOrganization;
    String introDocumentNumber;
    Date introDocumentDate;
    Date transferDate;
    @NotNull(message = "Không được để trống trường mã chi bộ chuyển đến")
    String organizationCode;
}
