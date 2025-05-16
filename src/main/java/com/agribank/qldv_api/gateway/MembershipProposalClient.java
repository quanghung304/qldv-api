package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldvutils.entity.MembershipProposal;
import com.agribank.qldvutils.entity.MembershipProposalDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "membershipProposalClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface MembershipProposalClient {
    @PostMapping("api/v1/membership-proposal/save/all")
    DefaultResponse<List<MembershipProposal>> saveAll(
            @RequestBody List<MembershipProposal> requests
    );

    @PostMapping("api/v1/membership-proposal/find-by-id/{id}")
    DefaultResponse<MembershipProposal> findById(
            @PathVariable(name = "id") String id
    );

    @PostMapping("api/v1/membership-proposal/search")
    DefaultResponse<PageResponse<MembershipProposalDtoResponse>> search(
            @RequestBody MPSearchDraftRequest requests
    );
}
