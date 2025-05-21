package com.agribank.qldv_api.request;
import com.agribank.qldvutils.exception.CommonException;
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
    String email;
    String fullName;
    String vneid;
    String phone;
    Integer staffCode;
    String address;
    Integer gender;
    String jobPosition;
    Integer depId;
    String userKind;
    @Builder.Default
    String password = "Agribank@123";
    List<String> roleIds;

    public void validate() {
        if (Objects.isNull(email)) {
            throw new CommonException("Email is required");
        }  else if (Objects.isNull(brcd)) {
            throw new CommonException("Brcd is required");
        }

        if (!validateEmail(email)){
            throw new CommonException("Không đúng định dạng email agribank. Vui lòng kiểm tra lại!");
        }

        if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
            throw new CommonException("Roleids is required");
        }
        this.email = email.trim().toLowerCase();
        if (Objects.isNull(fullName) || fullName.isBlank()) {
            throw new CommonException("FullName is required");
        }
    }

    private boolean validateEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_.+-]+@agribank\\.com\\.vn$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
    }
}
