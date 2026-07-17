package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @RequirePermission(function = "FN9", action = "CREATE")
    @PostMapping("/register")
    public ResponseEntity<DefaultResponse<UserResponse>> register(@RequestBody RegisterRequest request) {
        request.validate();
        return DefaultResponse.success(authenticationService.register(request));
    }
}
