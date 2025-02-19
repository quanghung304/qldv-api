package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldvutils.gateway.HttpClient;
import com.agribank.qldvutils.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class IAMGateway {
    @Value("${iam.api.url}")
    public String iamUrl;
    @Value("${iam.api.key}")
    public String xApiKey;
    private Map<String, String> headers = new HashMap<>();
    private String baseUrl = "";

    @Autowired
    public HttpClient httpClient;


    public HashMap<String, String> buildHeader() {
        HashMap<String, String> headers = new HashMap();
        headers.put("x-api-key", this.xApiKey);
        return headers;
    }

    @PostConstruct
    public void init() {
        headers = buildHeader();
        baseUrl = "api/v1";
    }

    public UserResponse register(IAMRegisterRequest registerRequest) {
        String url = String.format("%s/%s/auth/signup", iamUrl, baseUrl);
        ParameterizedTypeReference<DefaultResponse<UserResponse>> referenceType = new ParameterizedTypeReference<>() {};
        try {
            DefaultResponse<UserResponse> response = httpClient.post(url, headers, registerRequest, null, referenceType);
            return Objects.nonNull(response) ? response.getData() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public UserResponse verifyToken(String token) {
        String url = String.format("%s/%s/check/token", iamUrl, baseUrl);
        headers.put("Authorization", "Bearer " + token);
        BaseResponse<UserResponse> baseResponse = httpClient.get(url, headers, new ParameterizedTypeReference<>() {});
        return Objects.nonNull(baseResponse) ? baseResponse.getData() : null;
    }
}
