package com.agribank.qldv_api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class InputValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {

            //Duyệt toàn bộ parameter gửi lên
            Map<String, String[]> params = httpRequest.getParameterMap();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String fieldName = entry.getKey();
                String[] values = entry.getValue();

                if (values == null) continue;

                for (String val : values) {
                    if (val != null && !val.isEmpty()) {
                        try {
                            // gọi InputValidator mới (tự nhận kiểu dữ liệu)
                            InputValidator.validateObject(val);
                        } catch (SecurityException ex) {
                            // nếu phát hiện XSS hoặc dữ liệu không hợp lệ thì chặn request
                            throw new ServletException("Dữ liệu không hợp lệ ở trường '" + fieldName + "': " + ex.getMessage());
                        }
                    }
                }
            }
        }

        // Cho phép request đi tiếp nếu hợp lệ
        chain.doFilter(request, response);
    }
}
