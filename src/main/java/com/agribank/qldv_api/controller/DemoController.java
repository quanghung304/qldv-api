package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/check")
public class DemoController {
    @GetMapping("health")
    public ResponseEntity<DefaultResponse<String>> helloWorld() {
        return DefaultResponse.success("Hello from unsecured endpoint");
    }

    @GetMapping("/token")
    public ResponseEntity<DefaultResponse<String>> verifyToken() {
        return DefaultResponse.success("Hello from secured endpoint");
    }
}
