package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnifyOrganizationRequest extends BaseFormRequest {
    private static final Integer MAX_SIZE = 5;

    @NotNull(message = "không để trống trường mã tổ chức đảng nhận hợp nhất")
    private String organizationCode;
    private String organizationName;
    private String form;
    private List<String> unifyCodes;

    public void validate() {
        if (unifyCodes.isEmpty() || unifyCodes.size() > MAX_SIZE) {
            throw new CommonException("Số lượng chi, đảng bộ không hợp lệ");
        }

        unifyCodes.remove(organizationCode);
    }
}