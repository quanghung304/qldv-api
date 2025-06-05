package com.agribank.qldv_api.response.establishment_dissolve;

import com.agribank.qldv_api.response.BaseFormDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveResponse extends BaseFormDto {
    String id;
    String code;
    String name;
    String form;
    Integer type;
}
