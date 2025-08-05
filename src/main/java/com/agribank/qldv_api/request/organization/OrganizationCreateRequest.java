package com.agribank.qldv_api.request.organization;

import com.agribank.qldv_api.enums.Constants;
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
    //Mã tcd cấp trên
    String parentCode;


    @Override
    public void validate() {
        super.validate();
        //Chỉ truyền code khi tạo TCD cấp B
        if (Objects.nonNull(this.getCode()) && this.getCode().length() > Constants.FORM_B_NAME_LENGTH){
            throw new CommonException("Sai định dạng tổ chức Đảng, chỉ chứa 4 số");
        }
    }
}
