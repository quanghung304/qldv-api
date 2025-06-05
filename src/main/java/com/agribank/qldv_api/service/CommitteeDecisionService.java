package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.CommitteeDecisionClient;
import com.agribank.qldv_api.response.committee_decision.CommitteeDecisionResponse;
import com.agribank.qldvutils.entity.CommitteeDecision;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitteeDecisionService {
    private final CommitteeDecisionClient client;
    private final ModelMapper modelMapper;

    public List<CommitteeDecisionResponse> getAll() {
        List<CommitteeDecision> committeeDecisions = client.findAll().getData();
        if (committeeDecisions.isEmpty()) {
            return null;
        }

        return committeeDecisions.stream().map(
                c -> modelMapper.map(c, CommitteeDecisionResponse.class)
        ).toList();
    }

    public CommitteeDecision findByCode(String id) {
        return client.findById(id).getData().orElse(null);
    }
}
