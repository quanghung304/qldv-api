package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.gateway.report26.RemoveNamePartyDraftClient;
import com.agribank.qldvutils.entity.report26.RemoveNamePartyDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveNamePartyDraftService {

    private final RemoveNamePartyDraftClient client;

    public RemoveNamePartyDraft save(RemoveNamePartyDraft removeNamePartyDraft) {
        return client.save(removeNamePartyDraft).getData();
    }

    public RemoveNamePartyDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("RemoveNamePartyDraft not found"));
    }
}
