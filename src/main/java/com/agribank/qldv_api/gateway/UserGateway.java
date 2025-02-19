package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.gateway.DatabaseGateway;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserGateway extends DatabaseGateway {
    public User getUsersByEmail(String email) {

        String url = databaseUrl + "/api/v1/user/get-by-email";
        var headers = buildHeader();
        Map<String, String> params = new HashMap<>();
        params.put("email", email);

        DefaultResponse<User> response = httpClient.get(url, params, headers, new ParameterizedTypeReference<>() {});
        return Objects.nonNull(response) ? response.getData() : null;
    }

    public User save(User user) {

        String url = databaseUrl + "/api/v1/user/save";
        var headers = buildHeader();
        DefaultResponse<User> response = httpClient.post(url, headers, user, null, new ParameterizedTypeReference<>() {});
        return Objects.nonNull(response) ? response.getData() : null;
    }
}
