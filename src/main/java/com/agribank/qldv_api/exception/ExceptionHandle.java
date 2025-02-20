package com.agribank.qldv_api.exception;

import com.agribank.qldvutils.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<BaseResponse<Object>> exception(ValidationException exception) {
        return BaseResponse.error(exception.getMessage());
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Object>> exception(MissingServletRequestParameterException exception) {
        return BaseResponse.error("Không được để trống param " + exception.getParameterName());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BaseResponse<Object>> exception(Exception exception) {
        if (exception instanceof ClientAbortException) {
            // luồng hiện tại bị ngắt do call Thread.currentThread().interrupt()
            // => k xử lý, nếu k sẽ trả về đồng thời 2 response
            return null;
        }

        if (exception instanceof BadCredentialsException) {
            return BaseResponse.error("Tài khoản hoặc mật khẩu không đúng", HttpStatus.FORBIDDEN);
        }

        return BaseResponse.error(exception.getMessage());
    }
}