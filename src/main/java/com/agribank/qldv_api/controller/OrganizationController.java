package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationDetailResponse;
import com.agribank.qldv_api.response.organization.OrganizationListItemResponse;
import com.agribank.qldv_api.response.organization.OrganizationSubordinateResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.organization.CommitteeMemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @RequirePermission(function = "FN7", action = "VIEW")
    @PostMapping
    public ResponseEntity<BaseResponse<PageResponse<OrganizationListItemResponse>>> search(
            @RequestBody OrganizationSearchRequest request) {
        return BaseResponse.success(organizationService.search(request));
    }

    @RequirePermission(function = "FN7", action = "VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrganizationDetailResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(organizationService.getById(id));
    }

    @RequirePermission(function = "FN7", action = "VIEW")
    @GetMapping("/{id}/committee-members")
    public ResponseEntity<BaseResponse<List<CommitteeMemberResponse>>> getCommitteeMembers(@PathVariable String id) {
        return BaseResponse.success(organizationService.getCommitteeMembers(id));
    }

    @Operation(summary = "Tra cứu tổ chức đảng trực thuộc",
            description = "Trả về danh sách tất cả tổ chức đảng trực thuộc (con cháu ở mọi cấp trong cây tổ "
                    + "chức) của 1 tổ chức đảng, kèm cấp độ (level) tính từ CONNECT BY. Áp dụng cùng phạm vi "
                    + "dữ liệu (scope) theo role_code như các API /organizations khác.")
    @RequirePermission(function = "FN7", action = "VIEW")
    @GetMapping("/{organizationId}/subordinates")
    public ResponseEntity<BaseResponse<List<OrganizationSubordinateResponse>>> getSubordinates(
            @Parameter(description = "id (UUID) của tổ chức đảng gốc cần tra cứu danh sách trực thuộc")
            @PathVariable String organizationId) {
        return BaseResponse.success(organizationService.getSubordinates(organizationId));
    }
}
