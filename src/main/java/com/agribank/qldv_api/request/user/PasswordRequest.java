package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordRequest {
    String oldPassword;
    String newPassword;

    public void validate(){
        if (Objects.isNull(oldPassword)){
            throw new ValidationException("oldPassword is required");
        }

        if (Objects.isNull(newPassword)){
            throw new ValidationException("newPassword is required");
        }
    }
}
