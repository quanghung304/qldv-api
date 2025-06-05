package com.agribank.qldv_api.service.party_reinstatement;

import com.agribank.qldv_api.gateway.party_reinstatement.PartyReinstatementDraftClient;
import com.agribank.qldvutils.entity.party_reinstatement.PartyReinstatementDraft;
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
