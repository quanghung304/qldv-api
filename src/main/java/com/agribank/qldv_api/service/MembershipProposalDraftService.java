package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.MembershipProposalDraftClient;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembershipProposalDraftService {
    private final MembershipProposalDraftClient client;

    public MembershipProposalDraft save(MembershipProposalDraft deceasedDraft) {
        return client.save(deceasedDraft).getData();
    }

    public MembershipProposalDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("DeceasedDraft not found"));
    }


}
