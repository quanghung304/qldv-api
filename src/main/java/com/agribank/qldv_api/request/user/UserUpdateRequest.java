package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    Integer idIam;
    Integer brcd;
    Integer depId;
    String fullName;

    public void validate(){
        if (Objects.isNull(idIam)) {
            throw new ValidationException("userId is null");
        }
    }
}
