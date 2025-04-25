package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.role.RoleResponse;
import com.agribank.qldv_api.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role")
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/get-by-id")
    public ResponseEntity<DefaultResponse<RoleResponse>> getById(@RequestParam String id) {
        return DefaultResponse.success(roleService.findRoleById(id));
    }

    @GetMapping("/get-all-role")
    public ResponseEntity<DefaultResponse<List<RoleResponse>>> getAllRole() {
        return DefaultResponse.success(roleService.getAllRole());
    }
}
