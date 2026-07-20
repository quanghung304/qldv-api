package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.PageItemsResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchIamResponse;
import com.agribank.qldv_api.response.user.UserListResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldvutils.request.SearchUserRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('QLDV_SYSTEM_ADMIN')")
    @PostMapping("/iam-search")
    public ResponseEntity<DefaultResponse<UserSearchIamResponse>> search(@RequestBody SearchUserIAMRequest request) {
        return DefaultResponse.success(userService.searchUserIam(request));
    }

    @RequirePermission(function = "FN9", action = "VIEW")
    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<UserListResponse>>> search(
            @RequestBody SearchUserRequest request
    ) {
        request.validate();
        return DefaultResponse.success(userService.searchUsers(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<DefaultResponse<String>> changePassword(@RequestBody PasswordRequest request) {
        request.validate();
        return DefaultResponse.success(userService.changePassword(request), null);
    }

    @RequirePermission(function = "FN9", action = "EDIT")
    @PostMapping("/reset-password")
    public ResponseEntity<DefaultResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        request.validate();
        return DefaultResponse.success(userService.resetPassword(request), null);
    }

    @RequirePermission(function = "FN9", action = "EDIT")
    @PutMapping("/update")
    public ResponseEntity<DefaultResponse<String>> update(@RequestBody UserUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(userService.update(request), null);
    }

    @RequirePermission(function = "FN9", action = "DELETE")
    @PatchMapping("/{id}/status")
    public ResponseEntity<DefaultResponse<String>> active(@PathVariable("id") String id) {
        return DefaultResponse.success(userService.active(id), null);
    }

    @RequirePermission(function = "FN9", action = "DELETE")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponse<String>> delete(@PathVariable("id") String id) {
        return DefaultResponse.success(userService.delete(id), null);
    }

    @RequirePermission(function = "FN9", action = "VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<UserResponse>> getUserById(@PathVariable("id") String id) {
        return DefaultResponse.success(userService.getUserInfo(id));
    }

    @GetMapping("/info")
    public ResponseEntity<DefaultResponse<UserResponse>> getUserInfo() {
        return DefaultResponse.success(userService.getUserInfo(null));
    }


    @PutMapping("/update-user-requested")
    public ResponseEntity<DefaultResponse<String>> updateUserRequested(@RequestBody UserRequestedUpdate request) {
        return DefaultResponse.success(userService.updateUserRequested(request));
    }
}
