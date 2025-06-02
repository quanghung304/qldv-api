package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.MembershipProposal;
import com.agribank.qldvutils.request.membershipProposal.MPSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.membershipProposal.MembershipProposalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "membershipProposalClient",
        url = "${qldv.database.url}" + "/api/v1/membership-proposal",
        configuration = DatabaseFeignConfiguration.class)
public interface MembershipProposalClient extends BaseClient<MembershipProposal, String>{
    @PostMapping("/search")
    DefaultResponse<PageResponse<MembershipProposalResponse>> search(
            @RequestBody MPSearchRequest requests
    );
}
