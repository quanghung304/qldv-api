package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.config.InputValidator;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalSanitizeAdvice extends RequestBodyAdviceAdapter {
    private static final Pattern LARGE_NUMBER_PATTERN = Pattern.compile("\\b\\d{10,}\\b");

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {

        // Đọc toàn bộ JSON raw body trước khi Jackson parse
        String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);

        // Kiểm tra xem có số nào vượt giới hạn không
        var matcher = LARGE_NUMBER_PATTERN.matcher(body);
        if (matcher.find()) {
            throw new SecurityException("JSON chứa giá trị số quá lớn, có thể gây tràn kiểu dữ liệu");
        }

        // Nếu hợp lệ → trả body về cho Jackson parse như bình thường
        InputStream newInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return newInputStream;
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        InputValidator.validateObject(body);
        return body;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Áp dụng cho toàn bộ request body
    }
}
