package com.agribank.qldv_api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultResponse<T> {
    private Boolean success;
    private String message;
    private T data;

    public static <T> ResponseEntity<DefaultResponse<T>> success(T data) {
        return new ResponseEntity<>(DefaultResponse.<T>builder()
                .success(true)
                .data(data)
                .build(), HttpStatus.OK);
    }

    public static <T> ResponseEntity<DefaultResponse<T>> success(String message, T data) {
        return new ResponseEntity<>(DefaultResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build(), HttpStatus.OK);
    }

    public static <T> ResponseEntity<DefaultResponse<T>> error(String message) {
        return new ResponseEntity<>(DefaultResponse.<T>builder()
                .success(false)
                .message(message)
                .build(), HttpStatus.OK);
    }

    public static <T> ResponseEntity<DefaultResponse<T>> errorAccessDeny(String message) {
        return new ResponseEntity<>(DefaultResponse.<T>builder()
                .success(false)
                .message(message)
                .build(), HttpStatus.FORBIDDEN);
    }
}
