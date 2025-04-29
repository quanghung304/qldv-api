package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.user.ActiveUserIAMRequest;
import com.agribank.qldv_api.request.user.PasswordRequest;
import com.agribank.qldv_api.request.user.ResetPasswordRequest;
import com.agribank.qldv_api.request.user.UserIAMUpdate;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "iamClient", url = "${iam.api.url}", configuration = IamFeignConfiguration.class) // IAM service URL from properties
public interface IAMClient {
    @PostMapping("api/v1/auth/signup")
    DefaultResponse<UserIamResponse> register(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody IAMRegisterRequest request
    );

    @GetMapping("api/v1/check/token")
    DefaultResponse<UserIamResponse> verifyToken(
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

    @DeleteMapping("api/v1/user/remove-user-app")
    DefaultResponse<String> delete(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "app_id") Integer appId
    );

    @PutMapping("api/v1/user/change-password")
    DefaultResponse<String> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody PasswordRequest request
            );

    @PutMapping("api/v1/user/update-user")
    DefaultResponse<String> updateUserIAM(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UserIAMUpdate request
    );

    @PostMapping("api/v1/user/reset-password")
    DefaultResponse<String> resetPassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ResetPasswordRequest request
    );

    @PostMapping("api/v1/user/active")
    DefaultResponse<String> active(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody ActiveUserIAMRequest request
    );
}
