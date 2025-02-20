package com.agribank.qldv_api.request;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    String maSo;
    Integer brcd;
    String maSoTCD;
    String ten;
    String quyen;
    String chucVu;
    String tel;
    String email;

    public void validate() {
        if (Objects.isNull(email)) {
            throw new CommonException("Email is required");
        }  else if (Objects.isNull(brcd)) {
            throw new CommonException("Brcd is required");
        }

        if (!validateEmail(email)){
            throw new CommonException("Không đúng định dạng email agribank. Vui lòng kiểm tra lại!");
        }
        this.email = email.trim().toLowerCase();
    }

    private boolean validateEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_.+-]+@agribank\\.com\\.vn$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
    }
}
