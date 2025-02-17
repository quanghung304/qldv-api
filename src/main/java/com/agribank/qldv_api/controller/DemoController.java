package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<DefaultResponse<String>> helloWorld() {
        return DefaultResponse.success("Hello from secured endpoint");
    }
}
