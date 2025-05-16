package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldv_api.service.MembershipProposalService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/membership-proposal", produces = "application/json")
@RequiredArgsConstructor
public class MembershipProposalController {
    private final MembershipProposalService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<MembershipProposalDtoResponse>>> search(@RequestBody MPSearchDraftRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }
}
