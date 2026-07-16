package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.UserScope;
import com.agribank.qldvutils.entity.UserScopeId;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "userScopeClient", url = "${qldv.database.url}" + "/api/v1/user-scope", configuration = DatabaseFeignConfiguration.class)
public interface UserScopeClient extends BaseClient<UserScope, UserScopeId> {
}
