package com.agribank.qldv_api.request.form02;

import com.agribank.qldvutils.exception.CommonException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnifyOrgUpdateRequest extends UnifyOrganizationRequest{
    String id;

    @Override
    public void validate() {
        super.validate();
        if (Objects.isNull(id) || id.isBlank()) {
            throw new CommonException("Kiểm tra lại id");
        }
    }
}
