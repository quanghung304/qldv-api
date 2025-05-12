package com.agribank.qldv_api.response.establishmentDissolve;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveResponse {
    String id;
    String code;
    String name;
    String form;
    Integer type;
    //Số kết luận/nghị quyết
    String resolutionNumber;
    //Ngày kết luận/nghị quyết
    Date resolutionDate;
    //Số quyết định thành lập
    String establishmentDecisionNumber;
    //Ngày quyết định
    Date decisionDate;
    //Ngày hiệu lực
    Date effectiveDate;
}
