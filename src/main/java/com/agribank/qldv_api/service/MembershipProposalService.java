package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.MembershipProposalClient;
import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldvutils.entity.MembershipProposal;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipProposalService {
    private final MembershipProposalClient client;
    private final CheckAuthorityService checkAuthorityService;

    public void saveAll(List<MembershipProposal> proposals) {
        client.saveAll(proposals);
    }

    public MembershipProposal findById(String id) {
        return client.findById(id).getData();
    }

    public PageResponse<MembershipProposalDtoResponse> search(MPSearchDraftRequest request){
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        return client.search(request).getData();
    }
}
