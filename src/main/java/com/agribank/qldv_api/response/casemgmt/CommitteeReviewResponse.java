package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldvutils.entity.CaseCommitteeReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeReviewResponse {
    String caseId;
    String statusId;
    CaseCommitteeReview committeeReview;
}
