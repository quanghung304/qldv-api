package com.agribank.qldv_api.request.role;

import com.agribank.qldvutils.exception.CommonException;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleRequest {
    String userId;
    List<String> roleIds;

    public void validate(){
        if (Objects.isNull(userId)) {
            throw new CommonException("userId is null");
        }

        if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
            throw new CommonException("roleIds is null");
        }
    }
}