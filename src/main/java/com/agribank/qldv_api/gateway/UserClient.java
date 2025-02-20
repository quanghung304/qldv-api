package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.User;
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
}
