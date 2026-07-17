package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.RolePermission;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "rolePermissionClient", url = "${qldv.database.url}" + "/api/v1/role-permission", configuration = DatabaseFeignConfiguration.class)
public interface RolePermissionClient extends BaseClient<RolePermission, String> {
    @GetMapping("/check")
    DefaultListResponse<RolePermission> check(
            @RequestParam("roleCodes") List<String> roleCodes,
            @RequestParam("functionCode") String functionCode,
            @RequestParam("action") String action);
}
