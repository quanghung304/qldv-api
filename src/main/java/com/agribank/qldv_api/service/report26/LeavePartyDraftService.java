package com.agribank.qldv_api.service.report26;

import com.agribank.qldv_api.gateway.report26.LeavePartyDraftClient;
import com.agribank.qldvutils.entity.report26.LeavePartyDraft;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeavePartyDraftService {
    private final LeavePartyDraftClient client;

    public LeavePartyDraft save(LeavePartyDraft leavePartyDraft) {
        return client.save(leavePartyDraft).getData();
    }

    public LeavePartyDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("LeavePartyDraft not found"));
    }
}
