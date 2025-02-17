package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.AuthenticationRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.AuthenticationResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<DefaultResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        return DefaultResponse.success(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<DefaultResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return DefaultResponse.success(authenticationService .authenticate(request));
    }

    @GetMapping("/health")
    public ResponseEntity<DefaultResponse<String>> healthCheck() {
        return DefaultResponse.success("Hello from unsecured endpoint" );
    }
}
