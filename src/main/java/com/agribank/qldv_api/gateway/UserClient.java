package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.user.SearchUserRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "userClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface UserClient {
    @GetMapping("api/v1/user/get-by-email")
    DefaultResponse<User> getUserByEmail(
            @RequestParam String email
    );

    @PostMapping("api/v1/user/save")
    DefaultResponse<User> save(
            @RequestBody User user
    );

    @PostMapping("api/v1/user/search")
    DefaultResponse<PageResponse<User>> search(
            @RequestBody SearchUserRequest request
    );

    @GetMapping("api/v1/user/find-by-username")
    DefaultResponse<User> findByUsername(
            @RequestParam(name = "username") String username
    );

    @GetMapping("api/v1/user/find-by-id-iam")
    DefaultResponse<User> findByIdIAM(
            @RequestParam(name = "idIam") Integer idIam
    );

    @GetMapping("api/v1/user/find-by-id/{id}")
    DefaultResponse<User> findById(
            @PathVariable(name = "id") String id
    );

    @GetMapping("api/v1/user/get-info")
    DefaultResponse<UserDto> getUserInfo(
            @RequestParam(name = "email") String email
    );

    @GetMapping("api/v1/user/staff-code-and-organization-code")
    DefaultResponse<UserDto> userOrganization(
            @RequestParam(name = "staffCode") String staffCode,
            @RequestParam(name = "organizationCode") String organizationCode
    );
}
