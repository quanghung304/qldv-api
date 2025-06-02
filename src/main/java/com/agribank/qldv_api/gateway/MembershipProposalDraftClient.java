package com.agribank.qldv_api.gateway;

import org.springframework.cloud.openfeign.FeignClient;
import com.agribank.qldvutils.entity.MembershipProposalDraft;

@FeignClient(name = "membershipProposalDraftClient",
        url = "${qldv.database.url}" + "/api/v1/membership-proposal-draft",
        configuration = DatabaseFeignConfiguration.class)
public interface MembershipProposalDraftClient extends BaseClient<MembershipProposalDraft, String>{

}
