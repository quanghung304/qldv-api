package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.UserRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-role", url = "${qldv.database.url}" + "/api/v1/user-role", configuration = DatabaseFeignConfiguration.class)
public interface UserRoleClient extends BaseClient<UserRole, String> {

    @PostMapping("/find-by-id")
    DefaultResponse<List<UserRole>> getById(
            @RequestBody String userId
    );

    @DeleteMapping("/delete-by-id")
    DefaultResponse<String> deleteById(
            @RequestBody UserRoleRequest request
    );
}
