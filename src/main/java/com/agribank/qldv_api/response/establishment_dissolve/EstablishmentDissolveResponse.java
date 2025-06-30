package com.agribank.qldv_api.response.establishment_dissolve;

import com.agribank.qldv_api.response.BaseFormDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveResponse extends BaseFormDto {
    String id;
    String organizationCode;
    String name;
    String form;
    Integer type;
}
