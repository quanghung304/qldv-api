package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/organization", produces = "application/json")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<OrganizationResponse>>> search(@RequestBody @Valid OrganizationSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<OrganizationResponse>> get(@PathVariable(name = "code") String code) {
        return BaseResponse.success(service.get(code));
    }

    @GetMapping("/user")
    public ResponseEntity<BaseResponse<OrganizationResponse>> getOrganizationByUser(@RequestParam(name = "userId", required = false) String userId) {
        return BaseResponse.success(service.findByUserId(userId));
    }

    @GetMapping("/get-children")
    public ResponseEntity<BaseResponse<List<OrganizationResponse>>> getChildren(@RequestParam(name = "code") String code) {
        return BaseResponse.success(service.findByParentCode(code));
    }

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

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/delete/{code}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "code") String code) {
        return BaseResponse.success(service.createRequestDelete(code), null);
    }

    @GetMapping("/advisory-agency")
    public ResponseEntity<BaseResponse<List<OrganizationResponse>>> advisoryAgency() {
        return BaseResponse.success(service.getAdvisoryAgency());
    }

    @GetMapping("/party-branch")
    public ResponseEntity<BaseResponse<List<OrganizationResponse>>> partyBranch() {
        return BaseResponse.success(service.partyBranch());
    }
}
