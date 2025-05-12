package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.organizationReference.OrganizationReferenceResponse;
import com.agribank.qldv_api.service.OrganizationReferenceService;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/organization-reference", produces = "application/json")
@RequiredArgsConstructor
public class OrganizationReferenceController {
    private final OrganizationReferenceService service;

    @GetMapping("")
    public ResponseEntity<BaseResponse<List<OrganizationReferenceResponse>>> getAll() {
        return BaseResponse.success(service.getAll());
    }
}
