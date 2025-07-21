package com.agribank.qldv_api.response.form02.transfer;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyOrgTranDetailResponse {
    String organizationCode;
    String organizationName;
}
