package com.agribank.qldv_api.request.establishmentDissolve;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EReport01Type;
import com.agribank.qldv_api.request.validator.ValidOrganizationForm;
import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EstablishmentDissolveRequest extends BaseFormDto {
    String id;
    String organizationCode;
    String name;
    @ValidOrganizationForm(message = "Sai hình thức tổ chức đảng")
    String form;
    Integer type;

    public void validate(){
        super.validate();
        if (Objects.isNull(organizationCode)) {
            throw new CommonException("Code is required");
        }

        if (organizationCode.equals(Constants.BTCDU_CODE)) {
            throw new CommonException("Khong duoc giai the to chuc dang cap A");
        }

        if (EReport01Type.getValue(type) != EReport01Type.DISSOLVE.getId()) {
            throw new CommonException("Type is invalid");
        }
    }
}
