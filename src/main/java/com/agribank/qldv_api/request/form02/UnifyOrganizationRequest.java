package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    private List<UnifyDetailRequest> detailRequests;

    @Data
    public static class UnifyDetailRequest {
        @NotNull(message = "không để trống trường tổ chức đảng bi sáp nhap")
        private String unifiedCode;
        private List<String> staffCodes;
    }

    public void validate() {
        if (detailRequests.isEmpty() || detailRequests.size() > MAX_SIZE) {
            throw new CommonException("Số lượng chi, đảng bộ không hợp lệ");
        }
    }
}