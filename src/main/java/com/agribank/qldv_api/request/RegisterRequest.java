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
    String authType;

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

        if (Objects.isNull(authType) || authType.isBlank()) {
            throw new CommonException("Chưa chọn loại tài khoản thuộc Agribank hay công ty con");
        }

        if (!EAuthType.SSO_EMAIL.name().equals(authType) && !EAuthType.LOCAL_PASSWORD.name().equals(authType)) {
            throw new CommonException("Sai loại tài khoản");
        }
    }
}
