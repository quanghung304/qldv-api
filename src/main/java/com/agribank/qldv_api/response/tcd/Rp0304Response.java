package com.agribank.qldv_api.response.tcd;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rp0304Response {
    String code;
    String name;
    String form;
    Date establishmentDate;
    Date upgradeDate;
    Date downgradeDate;
}
