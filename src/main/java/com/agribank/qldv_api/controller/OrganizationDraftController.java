package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.organizationDraft.OrganizationDraftResponse;
import com.agribank.qldv_api.service.OrganizationDraftService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/organization-draft", produces = "application/json")
@RequiredArgsConstructor
public class OrganizationDraftController {
    private final OrganizationDraftService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<OrganizationResponse>> create(@RequestBody OrganizationCreateRequest request) {
        request.validate();
        return BaseResponse.success(service.create(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<OrganizationResponse>> update(@RequestBody OrganizationRequest request) {
        request.validate();
        return BaseResponse.success(service.update(request));
    }

    @PreAuthorize("hasAuthority('QLDV_APPROVER')")
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<List<DraftResponse>>> approve(@RequestBody List<DraftRequest> request) {
        return BaseResponse.success(service.approve(request));
    }

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<OrganizationDraftResponse>>> search(@RequestBody OrganizationDraftSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.delete(id), null);
    }
}
