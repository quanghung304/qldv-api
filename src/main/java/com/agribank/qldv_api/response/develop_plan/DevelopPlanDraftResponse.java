package com.agribank.qldv_api.response.develop_plan;

import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetailDraft;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevelopPlanDraftResponse {
    DevelopmentPlanDraft developPlan;
    List<DevelopmentPlanDetailDraft> planDetail;
}
