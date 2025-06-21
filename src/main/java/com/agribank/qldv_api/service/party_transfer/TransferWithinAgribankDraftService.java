package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank.TransferWithinAgribankDraftClient;
import com.agribank.qldvutils.entity.party_transfer.transfer_within_agribank.TransferWithinAgribankDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferWithinAgribankDraftService {
    private final TransferWithinAgribankDraftClient client;

    public TransferWithinAgribankDraft findById(String id) {
        return client.findById(id).getData().orElse(null);
    }

    public TransferWithinAgribankDraft save(TransferWithinAgribankDraft transferWithinAgribankDraft) {
        return client.save(transferWithinAgribankDraft).getData();
    }
}
