package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String id;
    Integer brcd;
    Integer depId;
    String fullName;
    List<String> roleIds;

    public void validate(){
        if (Objects.isNull(id)) {
            throw new ValidationException("id is null");
        }

        if (Objects.isNull(roleIds)) {
            this.roleIds = new ArrayList<>();
        }
    }
}
