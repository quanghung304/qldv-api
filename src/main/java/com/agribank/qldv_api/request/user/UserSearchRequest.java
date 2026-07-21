package com.agribank.qldv_api.request.user;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchRequest extends PagingRequest {
    private String name;
    private String organizationId;
    private Integer active;
    private Integer delete;
    private Integer brcd;
    private String roleId;
    private String status;
    private String keyword;
}
