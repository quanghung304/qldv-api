package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.request.membershipProposalDraft.MembershipProposalRequest;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldv_api.service.MembershipProposalDraftService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/membership-proposal-draft", produces = "application/json")
@RequiredArgsConstructor
public class MembershipProposalDraftController {
    private final MembershipProposalDraftService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody MembershipProposalRequest request) {
        request.validate();
        return BaseResponse.success(service.create(request));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<MembershipProposalDtoResponse>>> search(@RequestBody MPSearchDraftRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @PreAuthorize("hasAuthority('QLDV_APPROVER')")
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<List<DraftResponse>>> search(@RequestBody List<DraftRequest> requests) {
        return BaseResponse.success(service.approve(requests));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.delete(id), null);
    }
}
