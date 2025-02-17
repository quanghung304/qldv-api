package com.agribank.qldv_api.exception;

import com.agribank.qldv_api.response.DefaultResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<DefaultResponse<Object>> exception(ValidationException exception) {
        return DefaultResponse.error(exception.getMessage());
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<DefaultResponse<Object>> exception(MissingServletRequestParameterException exception) {
        return DefaultResponse.error("Không được để trống param " + exception.getParameterName());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<DefaultResponse<Object>> exception(Exception exception) {
        if (exception instanceof ClientAbortException) {
            // luồng hiện tại bị ngắt do call Thread.currentThread().interrupt()
            // => k xử lý, nếu k sẽ trả về đồng thời 2 response
            return null;
        }
        log.error("exception: ", exception);
        return DefaultResponse.error("Xảy ra lỗi, vui lòng thử lại sau");
    }

    @ExceptionHandler(value = MissingRequestHeaderException.class)
    public ResponseEntity<DefaultResponse<Object>> exception(MissingRequestHeaderException exception) {
        return new ResponseEntity<>(DefaultResponse.<Object>builder()
                .success(Boolean.FALSE)
                .message("Required header param " + exception.getHeaderName())
                .build(), HttpStatus.OK);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<DefaultResponse<Object>> exception(AccessDeniedException exception) {
        return DefaultResponse.errorAccessDeny(exception.getMessage());
    }
}
