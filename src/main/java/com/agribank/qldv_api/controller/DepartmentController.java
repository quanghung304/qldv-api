package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.DepartmentResponse;
import com.agribank.qldv_api.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/department")
public class DepartmentController {
    private final DepartmentService service;

    @GetMapping("")
    public ResponseEntity<DefaultResponse<List<DepartmentResponse>>> get(@RequestParam(name = "brcd") Integer brcd) {
        return DefaultResponse.success(service.get(brcd));
    }
}
