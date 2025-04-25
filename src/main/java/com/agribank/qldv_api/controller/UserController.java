package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/iam-search")
    public ResponseEntity<DefaultResponse<UserSearchResponse>> search(@RequestBody SearchUserIAMRequest request) {
        return DefaultResponse.success(userService.searchUserIam(request));
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<UserResponse>>> search(@RequestBody SearchUserRequest request) {
        request.validate();
        return DefaultResponse.success(userService.searchQLDV(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<DefaultResponse<String>> changePassword(@RequestBody PasswordRequest request) {
        request.validate();
        return DefaultResponse.success(userService.changePassword(request), null);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<DefaultResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        request.validate();
        return DefaultResponse.success(userService.resetPassword(request), null);
    }

    @PutMapping("/update")
    public ResponseEntity<DefaultResponse<String>> update(@RequestBody UserUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(userService.update(request), null);
    }
}
