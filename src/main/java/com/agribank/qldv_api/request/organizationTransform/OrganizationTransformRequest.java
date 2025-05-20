package com.agribank.qldv_api.request.organizationTransform;

import com.agribank.qldv_api.response.BaseFormDto;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationTransformRequest extends BaseFormDto {
    String organizationCode;
    String newName;
    String newForm;
}
