package com.agribank.qldv_api.request.organization;

import com.agribank.qldvutils.exception.CommonException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationCreateRequest extends OrganizationRequest {
    Integer brcd;
    //hình thức
    String form;
    //Mã tcd cấp trên
    String parentCode;


    @Override
    public void validate() {
        super.validate();
        if (Objects.isNull(form)){
            throw new CommonException("form is null");
        }
    }
}
