package com.agribank.qldv_api.config;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class JsonNumberValidator {
    // Public entry — không ép kiểu nào mặc định
    public void checkLargeNumbers(JsonNode root) {
        checkLargeNumbers(root, "$", Collections.emptyMap());
    }

    // Public entry — với map đường dẫn -> expected Type (ví dụ "$.user.age" -> Integer.class)
    public void checkLargeNumbers(JsonNode root, Map<String, Class<?>> expectedTypes) {
        checkLargeNumbers(root, "$", expectedTypes);
    }

    private void checkLargeNumbers(JsonNode node, String path, Map<String, Class<?>> expectedTypes) {
        // Nếu là text thì bỏ qua (theo yêu cầu)
        if (node.isTextual()) {
            return;
        }

        // Nếu là số thì check chi tiết
        if (node.isNumber()) {
            // Kiểm tra nếu có expected type cho đường dẫn này
            Class<?> expected = expectedTypes.get(path);

            if (node.isIntegralNumber()) {
                BigInteger bi = node.bigIntegerValue();

                if (expected != null) {
                    checkIntegralAgainstExpected(bi, expected, path);
                } else {
                    // Nếu không có expected type: chọn policy mặc định
                    // - fits int => ok
                    // - else fits long => ok
                    // - else → lỗi
                    if (bi.bitLength() <= 31) {
                        // fits int
                        return;
                    } else if (bi.bitLength() <= 63) {
                        // fits long
                        return;
                    } else {
                        throw new SecurityException(
                                String.format("JSON tại %s chứa số nguyên quá lớn cho long: %s", path, bi.toString())
                        );
                    }
                }
            } else { // floating / decimal
                BigDecimal bd = node.decimalValue();

                if (expected != null) {
                    checkDecimalAgainstExpected(bd, expected, path);
                } else {
                    // policy mặc định: cho phép nếu trong range double
                    BigDecimal maxDouble = BigDecimal.valueOf(Double.MAX_VALUE);
                    if (bd.abs().compareTo(maxDouble) > 0 || bd.scale() > 324) {
                        throw new SecurityException(
                                String.format("JSON tại %s chứa số thực vượt quá phạm vi double: %s", path, bd.toPlainString())
                        );
                    }
                }
            }
            return; // đã xử lý node số
        }

        // Nếu object -> duyệt field (cập nhật path)
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String childPath = path + "." + entry.getKey();
                checkLargeNumbers(entry.getValue(), childPath, expectedTypes);
            });
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode elem : node) {
                String childPath = path + "[" + i + "]";
                checkLargeNumbers(elem, childPath, expectedTypes);
                i++;
            }
        }
        // các loại khác (boolean, null) không cần xử lý
    }

    private void checkIntegralAgainstExpected(BigInteger bi, Class<?> expected, String path) {
        if (expected == Integer.class || expected == int.class) {
            BigInteger min = BigInteger.valueOf(Integer.MIN_VALUE);
            BigInteger max = BigInteger.valueOf(Integer.MAX_VALUE);
            if (bi.compareTo(min) < 0 || bi.compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s vượt phạm vi int.", path, bi.toString()));
            }
        } else if (expected == Long.class || expected == long.class) {
            BigInteger min = BigInteger.valueOf(Long.MIN_VALUE);
            BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
            if (bi.compareTo(min) < 0 || bi.compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s vượt phạm vi long.", path, bi.toString()));
            }
        } else if (expected == Short.class || expected == short.class) {
            BigInteger min = BigInteger.valueOf(Short.MIN_VALUE);
            BigInteger max = BigInteger.valueOf(Short.MAX_VALUE);
            if (bi.compareTo(min) < 0 || bi.compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s vượt phạm vi short.", path, bi.toString()));
            }
        } else if (expected == Byte.class || expected == byte.class) {
            BigInteger min = BigInteger.valueOf(Byte.MIN_VALUE);
            BigInteger max = BigInteger.valueOf(Byte.MAX_VALUE);
            if (bi.compareTo(min) < 0 || bi.compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s vượt phạm vi byte.", path, bi.toString()));
            }
        } else if (expected == BigInteger.class) {
            // luôn ok (BigInteger không giới hạn về magnitude trong Java)
            return;
        } else if (expected == Double.class || expected == double.class ||
                expected == Float.class || expected == float.class ||
                expected == BigDecimal.class) {
            // integral có thể cast sang float/double—chỉ kiểm tra range
            BigDecimal bd = new BigDecimal(bi);
            checkDecimalAgainstExpected(bd, expected, path);
        } else {
            // Unknown expected type -> fallback: ensure it fits in long
            if (bi.bitLength() > 63) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s quá lớn.", path, bi.toString()));
            }
        }
    }

    private void checkDecimalAgainstExpected(BigDecimal bd, Class<?> expected, String path) {
        if (expected == Double.class || expected == double.class) {
            BigDecimal max = BigDecimal.valueOf(Double.MAX_VALUE);
            if (bd.abs().compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: %s vượt Double.MAX_VALUE.", path, bd.toPlainString()));
            }
        } else if (expected == Float.class || expected == float.class) {
            BigDecimal max = BigDecimal.valueOf(Float.MAX_VALUE);
            if (bd.abs().compareTo(max) > 0) {
                throw new SecurityException(String.format("JSON tại %s: %s vượt Float.MAX_VALUE.", path, bd.toPlainString()));
            }
            // optional: kiểm tra precision/scale nếu bạn muốn
        } else if (expected == BigDecimal.class) {
            // luôn ok
            return;
        } else if (expected == Long.class || expected == long.class ||
                expected == Integer.class || expected == int.class) {
            // nếu expected là integral nhưng value là decimal -> lỗi hoặc có policy convert?
            try {
                BigInteger bi = bd.toBigIntegerExact(); // sẽ ném nếu có phần thập phân
                checkIntegralAgainstExpected(bi, expected, path);
            } catch (ArithmeticException ex) {
                throw new SecurityException(String.format("JSON tại %s: giá trị %s không phải số nguyên để ép sang %s.", path, bd.toPlainString(), expected.getSimpleName()));
            }
        } else {
            // fallback: đảm bảo nằm trong phạm vi double
            BigDecimal maxDouble = BigDecimal.valueOf(Double.MAX_VALUE);
            if (bd.abs().compareTo(maxDouble) > 0) {
                throw new SecurityException(String.format("JSON tại %s: %s quá lớn.", path, bd.toPlainString()));
            }
        }
    }
}
