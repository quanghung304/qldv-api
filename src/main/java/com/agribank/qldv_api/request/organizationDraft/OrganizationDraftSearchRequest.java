package com.agribank.qldv_api.request.organizationDraft;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationDraftSearchRequest extends PagingRequest {
    Integer approve;
    String code;
    String parentCode;
    String name;

    @Override
    public void validate() {
        super.validate();
    }
}
