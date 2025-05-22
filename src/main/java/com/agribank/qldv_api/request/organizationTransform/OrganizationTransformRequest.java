package com.agribank.qldv_api.request.organizationTransform;

import com.agribank.qldv_api.request.validator.ValidOrganizationForm;
import com.agribank.qldv_api.response.BaseFormDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationTransformRequest extends BaseFormDto {
    String organizationCode;
    String oldName;
    @ValidOrganizationForm(message = "Sai hình thức tổ chức đảng")
    String oldForm;
    String newName;
    @ValidOrganizationForm(message = "Sai hình thức tổ chức đảng")
    String newForm;
}
