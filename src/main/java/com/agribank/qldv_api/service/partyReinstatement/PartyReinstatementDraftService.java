package com.agribank.qldv_api.service.partyReinstatement;

import com.agribank.qldv_api.gateway.partyReinstatement.PartyReinstatementDraftClient;
import com.agribank.qldvutils.entity.partyReinstatement.PartyReinstatementDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyReinstatementDraftService {
    private final PartyReinstatementDraftClient client;

    public PartyReinstatementDraft save(PartyReinstatementDraft partyReinstatementDraft) {
        return client.save(partyReinstatementDraft).getData();
    }

    public PartyReinstatementDraft findById(String id) {
        return client.findById(id).getData().orElse(null);
    }
}
