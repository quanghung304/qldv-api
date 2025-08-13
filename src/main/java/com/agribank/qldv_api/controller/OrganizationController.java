package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.organization.OrganizationHierarchyResponse;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldvutils.dto.OrganizationDto;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldvutils.request.organization.OrganizationSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/import")
    public ResponseEntity<BaseResponse<String>> importExcel(@RequestParam(name = "file") MultipartFile file) {
        BaseResponse response = service.importExcel(file);
        return BaseResponse.success(response.getMessage(), null);
    }

    @GetMapping("/get-list-organization-code-name")
    public ResponseEntity<BaseResponse<List<OrganizationDto>>> getListOrganizationCodeName() {
        return BaseResponse.success(service.getListOrganizationCodeName());
    }

    @GetMapping("/get/all")
    public ResponseEntity<BaseResponse<List<OrganizationHierarchyResponse>>> getAll() {
        return BaseResponse.success(service.getAll());
    }

    @GetMapping("/get/form-b")
    public ResponseEntity<BaseResponse<List<OrganizationResponse>>> getOrganizationFormB() {
        return BaseResponse.success(service.getOrganizationFormB());
    }

    @GetMapping("filter-organizations")
    public ResponseEntity<DefaultListResponse<OrganizationResponse>> getChildOrganizationByUser() {
        return DefaultListResponse.success(service.getChildOrganizationByUser());
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft")
    public ResponseEntity<BaseResponse<OrganizationResponse>> updateDraft(@RequestBody OrganizationCreateRequest request) {
        request.validate();
        request.validateId();
        return BaseResponse.success(service.updateDraft(request));
    }

    @GetMapping("/draft")
    public ResponseEntity<BaseResponse<OrganizationResponse>> getDraftDetail(@RequestParam(name = "id") String id) {
        return BaseResponse.success(null, service.getDraftDetail(id));
    }
}
