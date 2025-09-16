package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    Integer appId;
    String username;
    String password;

    public void validate(){
        if (Objects.isNull(appId)){
            throw new ValidationException("appId is required");
        }

        if (Objects.isNull(username)){
            throw new ValidationException("username is required");
        }
    }
}
