package com.agribank.qldv_api.request.establishment_dissolve_draft;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EDDraftSearchRequest extends PagingRequest {
    Integer type;
    Integer status;
    String code;
}
