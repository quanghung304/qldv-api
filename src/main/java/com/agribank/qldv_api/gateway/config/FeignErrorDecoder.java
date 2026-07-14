package com.agribank.qldv_api.gateway.config;

import com.agribank.qldv_api.response.DefaultResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class FeignErrorDecoder
        implements ErrorDecoder
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Read the response body as a String
            String responseBody = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

            // Convert response JSON to DefaultResponse
            DefaultResponse<?> errorResponse = objectMapper.readValue(responseBody, new TypeReference<>() {});

            // Extract the message
            String errorMessage = errorResponse.getMessage();

            return new FeignException.FeignClientException(
                    response.status(), errorMessage, response.request(), responseBody.getBytes(), response.headers()
            );
        } catch (Exception e) {
            return new FeignException.FeignClientException(
                    response.status(), "Unknown error occurred", response.request(), new byte[0], response.headers()
            );
        }
    }
}
