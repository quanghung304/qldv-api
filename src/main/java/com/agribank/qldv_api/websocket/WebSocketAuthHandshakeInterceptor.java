package com.agribank.qldv_api.websocket;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldvutils.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Xác thực handshake WS/STOMP TRƯỚC khi nâng cấp kết nối — trình duyệt KHÔNG cho set header
 * Authorization tùy ý trên request WebSocket (giới hạn của WebSocket API, khác XHR thường), nên
 * client gửi token qua query param ({@code ?token=...}) thay vì header (SockJS giữ nguyên query
 * string qua mọi transport, kể cả fallback websocket thuần).
 *
 * Xác thực lại ĐÚNG luồng đã dùng cho HTTP ({@link com.agribank.qldv_api.jwt.JwtTokenFilter}):
 * gọi {@code IAMClient.verifyToken()} — client này đọc header Authorization từ
 * {@code RequestContextHolder} (xem {@code IamFeignConfiguration#IAMInterceptor}), nên phải tạm
 * bind 1 request "giả lập" thêm header Authorization từ token query param trước khi gọi, rồi
 * khôi phục request gốc ngay sau đó (finally) — không đụng gì tới request thật của handshake.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {
    public static final String USER_ID_ATTR = "userId";

    private final IAMClient iamClient;
    private final UserClient userClient;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || !(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        RequestAttributes previous = RequestContextHolder.getRequestAttributes();
        try {
            RequestContextHolder.setRequestAttributes(
                    new ServletRequestAttributes(wrapWithAuthorizationHeader(servletRequest.getServletRequest(), token)));

            UserIamResponse userIamResponse = iamClient.verifyToken().getData();
            if (userIamResponse == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            UserDto user = userClient.getUserInfo(userIamResponse.getEmail()).getData();
            if (user == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put(USER_ID_ATTR, user.getId());
            return true;
        } catch (Exception e) {
            log.warn("Xác thực WebSocket handshake thất bại: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } finally {
            RequestContextHolder.setRequestAttributes(previous);
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // không cần xử lý gì thêm sau handshake
    }

    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(kv -> kv.length == 2 && "token".equals(kv[0]))
                .map(kv -> URLDecoder.decode(kv[1], StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }

    private HttpServletRequest wrapWithAuthorizationHeader(HttpServletRequest original, String token) {
        return new HttpServletRequestWrapper(original) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return "Bearer " + token;
                }
                return super.getHeader(name);
            }
        };
    }
}
