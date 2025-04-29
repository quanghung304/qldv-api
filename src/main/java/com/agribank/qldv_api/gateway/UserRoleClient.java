package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.UserRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-role", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface UserRoleClient {

    @PostMapping("api/v1/user-role/find-by-id")
    DefaultResponse<List<UserRole>> getById(
            @RequestBody String userId
    );
    @PostMapping("api/v1/user-role/save-all")
    DefaultResponse<List<UserRole>> saveAll(
            @RequestBody List<UserRole> userRoles
    );
    @DeleteMapping("api/v1/user-role/delete-by-id")
    DefaultResponse<String> deleteById(
            @RequestBody UserRoleRequest request
    );
}
