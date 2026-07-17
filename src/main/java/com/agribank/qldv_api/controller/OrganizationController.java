package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationDetailResponse;
import com.agribank.qldv_api.response.organization.OrganizationListItemResponse;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.organization.CommitteeMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<BaseResponse<PageResponse<OrganizationListItemResponse>>> search(
            @RequestBody OrganizationSearchRequest request) {
        return BaseResponse.success(organizationService.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrganizationDetailResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(organizationService.getById(id));
    }

    @GetMapping("/{id}/committee-members")
    public ResponseEntity<BaseResponse<List<CommitteeMemberResponse>>> getCommitteeMembers(@PathVariable String id) {
        return BaseResponse.success(organizationService.getCommitteeMembers(id));
    }
}
