package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "iamClient", url = "${iam.api.url}", configuration = IamFeignConfiguration.class) // IAM service URL from properties
public interface IAMClient {
    @PostMapping("api/v1/auth/signup")
    DefaultResponse<UserResponse> register(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody IAMRegisterRequest request
    );

    @GetMapping("api/v1/check/token")
    DefaultResponse<UserResponse> verifyToken(
            @RequestHeader("Authorization") String authorizationHeader
    );
}
