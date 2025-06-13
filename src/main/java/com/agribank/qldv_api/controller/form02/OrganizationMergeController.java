package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.response.organization_draft.OrganizationDraftResponse;
import com.agribank.qldv_api.service.form02.OrganizationMergeService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.merge.OrganizationMerge;
import com.agribank.qldvutils.request.MergeFilterRequest;
import com.agribank.qldvutils.request.PagingRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/merge")
@RequiredArgsConstructor
public class OrganizationMergeController {
    private final OrganizationMergeService mergeService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> createMergeRequest(@RequestBody @Valid MergeOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(mergeService.createMergeRequest(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultListResponse<OrganizationMerge>> getList(@RequestBody MergeFilterRequest request) {
        return DefaultListResponse.success(mergeService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<OrganizationMergeResponse>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(mergeService.getDetail(id));
    }
}
