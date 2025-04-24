package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.ApiLog;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("api/v1/user/get-user-page")
    DefaultResponse<UserSearchResponse> search(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "appId") Integer appId,
            @RequestParam(name = "brcd") String brcd,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "prntbrcd") String prntbrcd,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size
    );
}
