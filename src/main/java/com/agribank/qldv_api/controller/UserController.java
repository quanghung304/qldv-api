package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.user.SearchUserRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<UserSearchResponse>> search(@RequestBody SearchUserRequest request) {
        return DefaultResponse.success(userService.search(request));
    }
}
