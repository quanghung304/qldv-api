package com.agribank.qldv_api.response.establishmentDissolve;

import com.agribank.qldv_api.response.BaseFormDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveResponse extends BaseFormDto {
    String id;
    String code;
    String name;
    String form;
    Integer type;
}
