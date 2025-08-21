package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.membership_proposal_draft.MembershipProposalRequest;
import com.agribank.qldv_api.service.MembershipProposalService;
import com.agribank.qldvutils.request.membershipProposal.MPSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.membershipProposal.MembershipProposalResponse;
import com.agribank.qldvutils.response.membershipProposal.MembershipProposalSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping(value = "api/v1/membership-proposal", produces = "application/json")
@RequiredArgsConstructor
public class MembershipProposalController {
    private final MembershipProposalService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<MembershipProposalSearchResponse>>> search(@RequestBody @Valid MPSearchRequest request) {
        return BaseResponse.success(service.search(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> create(@RequestBody @Valid MembershipProposalRequest request) {
        request.validate();
        return BaseResponse.success(service.create(request), null);
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createRequestDelete(id), null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<MembershipProposalResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<BaseResponse<MembershipProposalResponse>> getDraftDetail(@PathVariable String id) {
        return BaseResponse.success(service.getDraftDetail(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft")
    public ResponseEntity<BaseResponse<Object>> getDraftDetail(@RequestBody @Valid MembershipProposalRequest request) {
        request.validate();
        return BaseResponse.success(service.updateProposalDraft(request));
    }
}
