package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.PartyReinstatementDraftClient;
import com.agribank.qldvutils.entity.PartyReinstatementDraft;
import com.agribank.qldvutils.exception.CommonException;
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
        return client.findById(id).getData().orElseThrow(()->new CommonException("PartyReinstatementDraft not found"));
    }
}
