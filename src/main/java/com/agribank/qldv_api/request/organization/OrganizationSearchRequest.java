package com.agribank.qldv_api.request.organization;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationSearchRequest extends PagingRequest {
    String keyword;
    List<Integer> status;
    List<String> organizationTypeId;
    LocalDate decisionDateFrom;
    LocalDate decisionDateTo;
}
