package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.role.RoleSearchRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "roleClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface RoleClient {
    @GetMapping("api/v1/role/get-by-id")
    DefaultResponse<Role> findRoleById(
            @RequestParam String id
    );
    @GetMapping("api/v1/role/get-all-role")
    DefaultResponse<List<Role>> getAllRole();

    @GetMapping("api/v1/role/search")
    PageResponse<Role> searchRole(@RequestBody RoleSearchRequest request);
}
