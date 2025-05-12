package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.role.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/add")
    public ResponseEntity<DefaultResponse<List<String>>> addUserRole(@RequestBody List<UserRoleRequest> request) {
        return success(userRoleService.addUserRole(request));
    }

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/update")
    public ResponseEntity<DefaultResponse<List<String>>> updateUserRole(@RequestBody UserRoleRequest request) {
        return success(userRoleService.updateUserRole(request));
    }

    @PreAuthorize("@securityService.isBTCDUTeller(authentication)")
    @PostMapping("/assign")
    public ResponseEntity<DefaultResponse<String>> assign(@RequestBody UserRoleRequest request) {
        request.validate();
        return success(userRoleService.assignUserRole(request));
    }
}
