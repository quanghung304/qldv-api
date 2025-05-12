package com.agribank.qldv_api.request.organization;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationSearchRequest extends PagingRequest {
    String name;
    String status;
    String code;

    @Override
    public void validate() {
        super.validate();
    }
}
