package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.IamFeignConfiguration;
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
            @RequestBody IAMRegisterRequest request
    );

    @GetMapping("api/v1/auth/ad/check-user")
    DefaultResponse<ADResponse> checkAd(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "ldapType") Integer ldapType
    );

    @PostMapping("api/v1/user/register-validate")
    DefaultResponse<Boolean> registerValidate(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody IAMRegisterRequest request
    );

    @GetMapping("api/v1/check/token")
    DefaultResponse<UserIamResponse> verifyToken();

    @GetMapping("api/v1/user/get-user-page")
    DefaultResponse<UserSearchIamResponse> search(
            @RequestParam(name = "appId") Integer appId,
            @RequestParam(name = "brcd") String brcd,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "prntbrcd") String prntbrcd,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "size") Integer size
    );

    @DeleteMapping("api/v1/user/remove-user-app")
    DefaultResponse<String> delete(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "app_id") Integer appId
    );

    @PutMapping("api/v1/user/change-password")
    DefaultResponse<String> changePassword(
            @RequestBody PasswordRequest request
            );

    @PutMapping("api/v1/user/update-user")
    DefaultResponse<String> updateUserIAM(
            @RequestBody UserIAMUpdate request
    );

    @PostMapping("api/v1/user/reset-password")
    DefaultResponse<String> resetPassword(
            @RequestBody ResetPasswordRequest request
    );

    @PostMapping("api/v1/user/active")
    DefaultResponse<String> active(
            @RequestBody ActiveUserIAMRequest request
    );

    @PutMapping("api/v1/user/update")
    DefaultResponse<String> userUpdate(
            @RequestBody UserRequestedUpdate request
    );

    @PostMapping("api/v1/branch/filter-brcds")
    DefaultResponse<List<BranchResponse>> getBranchInfo(
            @RequestBody List<Integer> brcds
    );

    @PostMapping("api/v1/branch/get-brcd-child")
    DefaultResponse<List<BranchChildResponse>> getBranchChildInfo(
            @RequestBody List<Integer> brcds
    );

    @GetMapping("api/v1/branch")
    DefaultResponse<List<BranchResponse>> getAllBranch();

    @GetMapping("api/v1/departments")
    DefaultResponse<List<DepartmentResponse>> getDepartment(
            @RequestParam(name = "brcd") Integer brcd
    );
}
