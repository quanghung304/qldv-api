package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.request.SearchUserRequest;
import com.agribank.qldvutils.request.user.UserEntityRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.user.UserSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "userClient", url = "${qldv.database.url}" + "/api/v1/user", configuration = DatabaseFeignConfiguration.class)
public interface UserClient extends BaseClient<User, String> {
    @GetMapping("/get-by-email")
    DefaultResponse<User> getUserByEmail(
            @RequestParam String email
    );

    @PostMapping("/search")
    DefaultResponse<PageResponse<UserSearchResponse>> search(
            @RequestBody SearchUserRequest request
    );

    @GetMapping("/find-by-username")
    DefaultResponse<User> findByUsername(
            @RequestParam(name = "username") String username
    );

    @GetMapping("/find-by-id-iam")
    DefaultResponse<User> findByIdIAM(
            @RequestParam(name = "idIam") Integer idIam
    );
    @GetMapping("/get-info")
    DefaultResponse<UserDto> getUserInfo(
            @RequestParam(name = "email") String email
    );

    @GetMapping("/staff-code-and-organization-code")
    DefaultResponse<UserDto> userOrganization(
            @RequestParam(name = "staffCode") String staffCode,
            @RequestParam(name = "organizationCode") String organizationCode
    );

    @PostMapping("/save-entity")
    DefaultResponse<String> saveEntity(@RequestBody UserEntityRequest request);
}
