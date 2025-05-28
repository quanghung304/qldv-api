package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.gateway.report26.DeceasedDraftClient;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeceasedDraftService {
    private final DeceasedDraftClient client;

    public DeceasedDraft save(DeceasedDraft deceasedDraft) {
        return client.save(deceasedDraft).getData();
    }

    public DeceasedDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("DeceasedDraft not found"));
    }
}
