package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveUserRequest {
    String id;
    String type;

    public void validate(){
        if(Objects.isNull(id)){
            throw new ValidationException("id is null");
        }

        if (Objects.isNull(type)) {
            throw new ValidationException("type is required");
        }
    }
}
