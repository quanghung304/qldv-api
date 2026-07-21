package com.agribank.qldv_api.request;
import com.agribank.qldv_api.enums.EAuthType;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    Integer brcd;
    String username;
    String fullName;
    Integer depId;
    Integer staffCode;
    List<String> roleIds;
    String organizationId;

    public void validate() {
        if(Objects.isNull(username)) {
            throw new CommonException("Chưa nhập username");
        }

        if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
            throw new CommonException("Chưa chọn Chức năng");
        }
        if (Objects.isNull(staffCode)) {
            throw new CommonException("Chưa nhập mã nhân viên");
        }
        if (Objects.isNull(fullName) || fullName.isBlank()) {
            throw new CommonException("Họ tên không được để trống");
        }

        if (Objects.isNull(organizationId)) {
            throw new CommonException("Chưa chọn Tổ chức Đảng");
        }
    }
}
