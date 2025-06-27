package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MergeOrganizationRequest extends BaseFormRequest {
    private static final Integer MAX_SIZE = 5;

    private String id;
    @NotNull(message = "không để trống trường mã tổ chức đảng nhận sáp nhập")
    private String organizationCode;
    private List<String> mergedCodes;

    public void validate() {
        if (mergedCodes.isEmpty() || mergedCodes.size() > MAX_SIZE) {
            throw new CommonException("Số lượng chi, đảng bộ không hợp lệ");
        }

        mergedCodes.remove(organizationCode);
    }
}
