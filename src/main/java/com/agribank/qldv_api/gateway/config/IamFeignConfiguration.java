package com.agribank.qldv_api.gateway.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamFeignConfiguration {
    @Value("${iam.api.key}")
    public String iamApiKey;
    @Bean
    public RequestInterceptor IAMInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-API-KEY", iamApiKey);
        };
    }
}
