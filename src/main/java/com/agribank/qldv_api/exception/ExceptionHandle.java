package com.agribank.qldv_api.exception;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {

    @ExceptionHandler(value = CommonException.class)
    public ResponseEntity<BaseResponse<Object>> exception(CommonException exception) {
        return BaseResponse.error(exception.getMessage());
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<BaseResponse<Object>> exception(ForbiddenException exception) {
        return BaseResponse.error(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> exception(NotFoundException exception) {
        return BaseResponse.error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Object>> exception(MissingServletRequestParameterException exception) {
        return BaseResponse.error("Không được để trống param " + exception.getParameterName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return DefaultResponse.error("Validation failed", errors);
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<DefaultResponse<Map<String, String>>> exception(FieldValidationException exception) {
        return DefaultResponse.error("Validation failed", exception.getFieldErrors());
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

        if (exception instanceof NoResourceFoundException) {
            return BaseResponse.error("kiểm tra lại đường dẫn api", HttpStatus.BAD_REQUEST);
        }

        return BaseResponse.error(exception.getMessage());
    }
}