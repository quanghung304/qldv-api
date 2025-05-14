package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/branch")
public class BranchController {
    private final BranchService service;

    @GetMapping("/all")
    public ResponseEntity<DefaultResponse<List<BranchResponse>>> getAll() {
        return DefaultResponse.success(service.getAll());
    }
}
