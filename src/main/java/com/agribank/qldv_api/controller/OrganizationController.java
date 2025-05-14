package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.organization.OrganizationSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.service.OrganizationService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/organization", produces = "application/json")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<OrganizationResponse>>> search(@RequestBody OrganizationSearchRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<OrganizationResponse>> get(@PathVariable(name = "code") String code) {
        return BaseResponse.success(service.get(code));
    }

    @GetMapping("/user")
    public ResponseEntity<BaseResponse<OrganizationResponse>> getOrganizationByUser(@RequestParam(name = "userId") String userId) {
        return BaseResponse.success(service.findByUserId(userId));
    }

    @GetMapping("/get-children")
    public ResponseEntity<BaseResponse<List<OrganizationResponse>>> getChildren(@RequestParam(name = "code") String code) {
        return BaseResponse.success(service.findByParentCode(code));
    }
}
