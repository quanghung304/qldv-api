package com.agribank.qldv_api.gateway.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class DatabaseFeignConfiguration {
    @Value("${qldv.database.api_key}")
    public String apiKey;
    @Bean
    public RequestInterceptor IAMInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-API-KEY", apiKey);
        };
    }
}
