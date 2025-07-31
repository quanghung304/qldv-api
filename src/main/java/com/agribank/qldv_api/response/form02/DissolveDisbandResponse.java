package com.agribank.qldv_api.response.form02;

import com.agribank.qldv_api.response.BaseFormDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DissolveDisbandResponse extends BaseFormDto {
    String id;
    String organizationCode;
    String name;
    String form;
    Integer type;
}
