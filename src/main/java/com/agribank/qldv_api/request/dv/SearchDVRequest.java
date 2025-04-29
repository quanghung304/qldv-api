package com.agribank.qldv_api.request.dv;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchDVRequest extends PagingRequest {
    String name;
    Integer brcd;
    String organizationCode;
    String parentCode;
    String vneid;
    String code;

    @Override
    public void validate() {
        super.validate();
    }
}
