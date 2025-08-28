package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.service.form02.OrganizationMergeService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.PageResponse;
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
    public ResponseEntity<DefaultResponse<PageResponse<OrganizationMerResponse>>> getList(@RequestBody SearchOrganizationUnionRequest request) {
        return DefaultResponse.success(mergeService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<OrganizationMergeResponse>> getDetail(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(mergeService.getDetail(id));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<DefaultResponse<OrganizationMergeResponse>> getDraftDetail(@PathVariable String id) {
        return DefaultResponse.success(mergeService.getDraftDetail(id));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> updateMergeRequest(@RequestBody @Valid MergeOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(mergeService.update(request));
    }

    @PutMapping("update-draft")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<String>> updateDraft(@RequestBody @Valid MergeOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(mergeService.updateDraft(request));
    }
}
