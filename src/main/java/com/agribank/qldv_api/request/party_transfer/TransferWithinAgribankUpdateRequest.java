package com.agribank.qldv_api.request.party_transfer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferWithinAgribankUpdateRequest extends TransferWithinAgribankRequest{
    Date expectedExpiryDate;
    Date transferDate;
    String receivingOrgBCode;
    String receivingOrgCCode;
}
