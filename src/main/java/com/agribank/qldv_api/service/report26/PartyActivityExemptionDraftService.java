package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.gateway.report26.PartyActivityExemptionDraftClient;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyActivityExemptionDraftService {
    private final PartyActivityExemptionDraftClient client;

    public PartyActivityExemptionDraft save(PartyActivityExemptionDraft draft) {
        return client.save(draft).getData();
    }

    public PartyActivityExemptionDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("PartyActivityExemptionDraft not found"));
    }
}
