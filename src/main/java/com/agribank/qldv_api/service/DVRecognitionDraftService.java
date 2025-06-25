package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.DVRecognitionDraftClient;
import com.agribank.qldvutils.entity.DVRecognitionDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DVRecognitionDraftService {
    private final DVRecognitionDraftClient client;

    public DVRecognitionDraft save(DVRecognitionDraft deceasedDraft) {
        return client.save(deceasedDraft).getData();
    }

    public DVRecognitionDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("DeceasedDraft not found"));
    }
}
