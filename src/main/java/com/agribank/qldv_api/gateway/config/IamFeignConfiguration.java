package com.agribank.qldv_api.gateway.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class IamFeignConfiguration {
    @Value("${iam.api.key}")
    public String iamApiKey;
    @Bean
    public RequestInterceptor IAMInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-API-KEY", iamApiKey);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authorizationHeader = attributes.getRequest().getHeader("Authorization");
                if (authorizationHeader != null) {
                    requestTemplate.header("Authorization", authorizationHeader);
                }
            }
        };
    }
}
