package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.gateway.development_plan.DevelopmentPlanDetailDraftClient;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlanDetailDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevelopmentPlanDetailDraftService {
    private final DevelopmentPlanDetailDraftClient client;

    public DevelopmentPlanDetailDraft save(final DevelopmentPlanDetailDraft draft) {
        return client.save(draft).getData();
    }

    public List<DevelopmentPlanDetailDraft> saveAll(final List<DevelopmentPlanDetailDraft> drafts) {
        return client.saveAll(drafts).getData();
    }

    public DevelopmentPlanDetailDraft findById(String id) {
        return client.findById(id).getData().orElse(null);
    }

    public List<DevelopmentPlanDetailDraft> findByRefId(String refId) {
        return client.findByRefId(refId).getData();
    }
}
