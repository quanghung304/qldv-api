package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.role.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.agribank.qldv_api.response.DefaultResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-role")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping("/add")
    public ResponseEntity<DefaultResponse<List<String>>> addUserRole(@RequestBody List<UserRoleRequest> request) {
        return success(userRoleService.addUserRole(request));
    }
    @PostMapping("/update")
    public ResponseEntity<DefaultResponse<List<String>>> updateUserRole(@RequestBody UserRoleRequest request) {
        return success(userRoleService.updateUserRole(request));
    }
}
