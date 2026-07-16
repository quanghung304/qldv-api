package com.agribank.qldv_api.request.user;

import com.agribank.qldv_api.enums.EUserStatus;
import com.agribank.qldv_api.exception.ValidationException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveUserRequest {
    String id;
    String accountStatus;

    public void validate(){
        if(Objects.isNull(id)){
            throw new ValidationException("Thiếu userId");
        }

        if (Objects.isNull(accountStatus)) {
            throw new ValidationException("Thếu trạng thái user");
        }

        if (!EUserStatus.ACTIVE.name().equals(accountStatus) && !EUserStatus.INACTIVE.name().equals(accountStatus)) {
            throw new ValidationException("Truyền sai giá trị");
        }
    }
}
