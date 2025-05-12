package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftRequest;
import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.response.organizationDraft.OrganizationDraftApproveResponse;
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

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<OrganizationResponse>> create(@RequestBody OrganizationCreateRequest request) {
        request.validate();
        return BaseResponse.success(service.create(request));
    }

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<OrganizationResponse>> update(@RequestBody OrganizationRequest request) {
        request.validate();
        return BaseResponse.success(service.update(request));
    }

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/approve")
    public ResponseEntity<BaseResponse<List<OrganizationDraftApproveResponse>>> approve(@RequestBody List<OrganizationDraftRequest> request) {
        return BaseResponse.success(service.approve(request));
    }

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<OrganizationDraftResponse>>> search(@RequestBody OrganizationDraftSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }
}
