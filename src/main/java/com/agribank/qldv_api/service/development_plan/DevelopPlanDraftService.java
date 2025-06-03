package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.gateway.development_plan.DevelopmentPlanDraftClient;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevelopPlanDraftService {
    private final DevelopmentPlanDraftClient client;

    public DevelopmentPlanDraft save (DevelopmentPlanDraft draft) {
        return client.save(draft).getData();
    }

    public DevelopmentPlanDraft findById(String id) {
        return client.findById(id).getData().orElse(null);
    }
}
