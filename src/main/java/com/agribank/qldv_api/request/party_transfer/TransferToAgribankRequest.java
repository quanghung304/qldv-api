package com.agribank.qldv_api.request.party_transfer;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferToAgribankRequest {
    @NotNull(message = "Không được để trống trường mã cán bộ")
    String staffCode;
    String fullName;
    String decisionNumber;
    Date issueDate;
    Date effectiveDate;
    String issuingOrganization;
    Date expectedExpiryDate;
    String firstIntroNumber;
    Date firstIntroDate;
    String transferringPartyName;
    String secondIntroNumber;
    Date transferDate;
    @NotNull(message = "Không được để trống trường mã đảng bộ nhận sinh hoạt")
    String receivingOrgBCode;
}
