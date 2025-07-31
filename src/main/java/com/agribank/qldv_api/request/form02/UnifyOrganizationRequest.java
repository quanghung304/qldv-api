package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnifyOrganizationRequest extends BaseFormRequest {
    static final Integer MAX_SIZE = 5;

    @NotNull(message = "không để trống trường mã tổ chức đảng nhận hợp nhất")
    @NotBlank(message = "không để trống trường mã tổ chức đảng nhận hợp nhất")
    String organizationCode;
    String organizationName;
    String form;
    List<String> unifyCodes;
    List<String> staffCodes;

    @Override
    public void validate() {
        if (unifyCodes.isEmpty() || unifyCodes.size() > MAX_SIZE) {
            throw new CommonException("Số lượng chi, đảng bộ không hợp lệ");
        }

        unifyCodes.remove(organizationCode);

        if (Objects.isNull(staffCodes)){
            staffCodes = new ArrayList<>();
        }
    }
}