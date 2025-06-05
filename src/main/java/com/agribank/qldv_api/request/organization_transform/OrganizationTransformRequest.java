package com.agribank.qldv_api.request.organization_transform;

import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.request.validator.ValidOrganizationForm;
import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationTransformRequest extends BaseFormDto {
    Integer type;
    String organizationCode;
    @NotNull
    String oldName;
    @ValidOrganizationForm(message = "Sai hình thức tổ chức đảng")
    String oldForm;
    String newName;
    @ValidOrganizationForm(message = "Sai hình thức tổ chức đảng")
    String newForm;

    public void validate(){
        super.validate();
        if (Objects.isNull(organizationCode)) {
            throw new CommonException("Code is required");
        }

        if (Objects.isNull(type) || EReport01Type.getValue(type) != EReport01Type.UPGRADE.getId()
                && EReport01Type.getValue(type) != EReport01Type.DOWNGRADE.getId()) {
            throw new CommonException("Type is invalid");
        }
    }
}
