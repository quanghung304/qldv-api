package com.agribank.qldv_api.exception;

import java.util.Map;

/**
 * Gom nhiều lỗi validate/business rule mức "Chặn" (ERR-SC02-xx...) thành 1 map field->message,
 * thay vì chỉ trả lỗi đầu tiên gặp được — theo đúng khuyến nghị response contract (SC-02 mục 6:
 * "Response nên trả list các lỗi, không chỉ 1 lỗi đầu tiên"). Render cùng khuôn dạng với
 * {@code MethodArgumentNotValidException} (ExceptionHandle) để FE xử lý thống nhất 1 chỗ.
 */
public class FieldValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public FieldValidationException(Map<String, String> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
