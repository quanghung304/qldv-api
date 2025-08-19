package com.agribank.qldv_api.request.develop_plan;

import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlan;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetailDraft;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRequestDraft {
    DevelopPlanDetailUpdateRequest request;
    DevelopmentPlanDraft developmentPlanDraft;
    List<DevelopmentPlanDetailDraft> developmentPlanDetailDraft;
    Request requestDetail;
}
