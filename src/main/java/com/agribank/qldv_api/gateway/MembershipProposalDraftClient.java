package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldvutils.entity.OrganizationDraft;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.MembershipProposalDraft;

import java.util.List;

@FeignClient(name = "membershipProposalDraftClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface MembershipProposalDraftClient {
    @GetMapping("api/v1/membership-proposal-draft/find-by-id/{id}")
    DefaultResponse<MembershipProposalDraft> findById(
            @PathVariable(name = "id") String id
    );

    @PostMapping("api/v1/membership-proposal-draft/save/all")
    DefaultResponse<List<MembershipProposalDraft>> saveAll(
            @RequestBody List<MembershipProposalDraft> requests
    );

    @PostMapping("api/v1/membership-proposal-draft/save")
    DefaultResponse<MembershipProposalDraft> save(
            @RequestBody MembershipProposalDraft requests
    );

    @DeleteMapping("api/v1/membership-proposal-draft/delete-by-id/{id}")
    DefaultResponse<MembershipProposalDraft> delete(
            @PathVariable(name = "id") String id
    );

    @PostMapping("api/v1/membership-proposal-draft/search")
    DefaultResponse<PageResponse<MembershipProposalDtoResponse>> search(
            @RequestBody MPSearchDraftRequest requests
    );

    @PostMapping("api/v1/membership-proposal-draft/find-all-by-id")
    DefaultResponse<List<MembershipProposalDraft>> findAllById(
            @RequestBody List<String> ids
    );
}
