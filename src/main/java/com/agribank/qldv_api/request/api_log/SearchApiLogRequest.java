package com.agribank.qldv_api.request.api_log;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchApiLogRequest extends PagingRequest {
    String objectReference;
    String dataType;
    Date fromDate;
    Date toDate;
    Integer brcd;
    String email;

    @Override
    public void validate() {
        super.validate();
    }
}
