package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.category.CaseTypeResponse;
import com.agribank.qldv_api.response.category.DocumentTypeResponse;
import com.agribank.qldv_api.response.category.OrganizationTypeResponse;
import com.agribank.qldv_api.response.category.StatusResponse;
import com.agribank.qldv_api.service.CategoryService;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/organization-types")
    public ResponseEntity<BaseResponse<List<OrganizationTypeResponse>>> getOrganizationTypes() {
        return BaseResponse.success(categoryService.getOrganizationTypes());
    }

    @GetMapping("/case-types")
    public ResponseEntity<BaseResponse<List<CaseTypeResponse>>> getCaseTypes() {
        return BaseResponse.success(categoryService.getCaseTypes());
    }

    @GetMapping("/statuses")
    public ResponseEntity<BaseResponse<List<StatusResponse>>> getStatuses() {
        return BaseResponse.success(categoryService.getStatuses());
    }

    @GetMapping("/document-types")
    public ResponseEntity<BaseResponse<List<DocumentTypeResponse>>> getDocumentTypes() {
        return BaseResponse.success(categoryService.getDocumentTypes());
    }
}
