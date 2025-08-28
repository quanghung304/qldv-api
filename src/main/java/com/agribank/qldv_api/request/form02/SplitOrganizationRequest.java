package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class SplitOrganizationRequest extends BaseFormRequest {
    private static final Integer MAX_SIZE = 5;

    @NotNull(message = "không để trống trường mã tổ chức đảng cũ")
    private String oldCode;
    private List<SplitDetailRequestNew> detailRequestsNew;
    private List<SplitDetailRequestOld> detailRequestsOld;

    @Data
    public static class SplitDetailRequestNew {
        @NotNull(message = "không để trống trường mã tổ chức đảng mới")
        private String newCode;
        @NotNull(message = "không để trống trường tên tổ chức đảng mới")
        private String newName;
        @NotNull(message = "không để trống trường hình thức tổ chức đảng mới")
        private String form;
        private List<String> members;
    }

    @Data
    public static class SplitDetailRequestOld {
        @NotNull(message = "không để trống trường mã tổ chức đảng mới")
        private String organizationCode;
        private List<String> members;
    }

    public void validate() {
        if (detailRequestsNew.isEmpty() || detailRequestsNew.size() > MAX_SIZE) {
            throw new CommonException("Số lượng chi, đảng bộ không hợp lệ");
        }
    }
}
