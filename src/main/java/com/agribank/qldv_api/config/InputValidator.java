package com.agribank.qldv_api.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Validator kiểm tra input user, phát hiện XSS / ký tự bất thường và chặn request
 */
public class InputValidator {

    private static final Pattern WHITELIST_PATTERN =
            Pattern.compile("^[\\p{L}0-9 _.,;:!?@#\\$%&()\\[\\]\\-]*$");

    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*script.*?>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onmouseover\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onfocus\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<iframe", Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("&lt;\\s*script\\s*&gt;", Pattern.CASE_INSENSITIVE),
            Pattern.compile("%3Cscript%3E", Pattern.CASE_INSENSITIVE)
    );

    /**Validate 1 chuỗi đầu vào */
    public static void validate(String input) {
        if (input == null) return;
        String value = input.trim();

        // Phát hiện XSS
        for (Pattern p : XSS_PATTERNS) {
            if (p.matcher(value).find()) {
                throw new SecurityException("Trường '" +  "' chứa nội dung nguy hiểm (XSS).");
            }
        }

        // Kiểm tra ký tự không hợp lệ
        if (!WHITELIST_PATTERN.matcher(value).matches()) {
            throw new SecurityException("Trường '" +  "' chứa ký tự không hợp lệ.");
        }

        //  Kiểm tra tràn số nếu toàn bộ ký tự là số
        if (value.matches("^-?\\d+$")) {
            try {
                new BigInteger(value); // test parse để đảm bảo không tràn
            } catch (NumberFormatException ex) {
                throw new SecurityException("Trường '" + "' có giá trị số vượt giới hạn cho phép.");
            }
        }

        //  Kiểm tra tràn số thập phân
        if (value.matches("^-?\\d+(\\.\\d+)?$")) {
            try {
                new BigDecimal(value);
            } catch (NumberFormatException ex) {
                throw new SecurityException("Trường '" + "' có giá trị số thập phân không hợp lệ.");
            }
        }
    }

    /**Duyệt đệ quy toàn bộ object */
    public static void validateObject(Object obj) {
        if (obj == null) return;

        Class<?> cls = obj.getClass();

        if (cls.isPrimitive() ||
                Number.class.isAssignableFrom(cls) ||
                Boolean.class.isAssignableFrom(cls) ||
                Character.class.isAssignableFrom(cls) ||
                cls.isEnum()) {
            return;
        }

        if (obj instanceof String s) {
            validate(s);
            return;
        }

        if (obj instanceof Collection<?> col) {
            for (Object item : col) validateObject(item);
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (key instanceof String s) validate(s);
                validateObject(val);
            }
            return;
        }

        if (cls.isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) validateObject(Array.get(obj, i));
            return;
        }

        for (Field f : cls.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(obj);
                if (val == null) continue;
                if (val instanceof String s) {
                    validate(s);
                } else {
                    validateObject(val);
                }
            } catch (IllegalAccessException ignored) {}
        }
    }
}