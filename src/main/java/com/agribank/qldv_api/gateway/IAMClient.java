package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchIamResponse;
import com.agribank.qldv_api.response.branch.BranchChildResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.response.user.ADResponse;
import com.agribank.qldv_api.response.user.DepartmentResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "iamClient", url = "${iam.api.url}", configuration = IamFeignConfiguration.class) // IAM service URL from properties
public interface IAMClient {
    @PostMapping("api/v1/auth/signup")
    DefaultResponse<UserIamResponse> register(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody IAMRegisterRequest request
    );

    @GetMapping("api/v1/auth/ad/check-user")
    DefaultResponse<ADResponse> checkAd(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "ldapType") Integer ldapType
    );

    @GetMapping("api/v1/check/token")
    DefaultResponse<UserIamResponse> verifyToken(
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("api/v1/user/get-user-page")
    DefaultResponse<UserSearchIamResponse> search(
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

    @PostMapping("api/v1/branch/filter-brcds")
    DefaultResponse<List<BranchResponse>> getBranchInfo(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody List<Integer> brcds
    );

    @PutMapping("api/v1/user/update")
    DefaultResponse<String> userUpdate(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody UserRequestedUpdate request
    );

    @PostMapping("api/v1/branch/get-brcd-child")
    DefaultResponse<List<BranchChildResponse>> getBranchChildInfo(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody List<Integer> brcds
    );

    @GetMapping("api/v1/branch")
    DefaultResponse<List<BranchResponse>> getAllBranch(
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("api/v1/departments")
    DefaultResponse<List<DepartmentResponse>> getDepartment(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "brcd") Integer brcd
    );
}
