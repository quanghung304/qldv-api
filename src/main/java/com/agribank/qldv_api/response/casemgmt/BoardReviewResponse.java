package com.agribank.qldv_api.response.casemgmt;

import com.agribank.qldvutils.entity.CaseBoardReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardReviewResponse {
    String caseId;
    String statusId;
    CaseBoardReview boardReview;
}
