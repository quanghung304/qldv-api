package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseDetailResponse extends CaseListItemResponse {
    List<CaseOrganizationSummaryResponse> organizations;
}
