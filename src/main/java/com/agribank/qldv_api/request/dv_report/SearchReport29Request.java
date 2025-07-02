package com.agribank.qldv_api.request.dv_report;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchReport29Request extends PagingRequest {
    String organizationCode;
    Date fromDate;
    Date toDate;
    Integer type;
}
