package com.agribank.qldv_api.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Gán Principal cho STOMP session từ userId đã xác thực ở
 * {@link WebSocketAuthHandshakeInterceptor#USER_ID_ATTR} — BẮT BUỘC để
 * {@code SimpMessagingTemplate.convertAndSendToUser(userId, ...)} định tuyến đúng session (Spring
 * dùng {@code Principal.getName()} làm key tra session theo user).
 */
public class UserPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object userId = attributes.get(WebSocketAuthHandshakeInterceptor.USER_ID_ATTR);
        return userId == null ? null : new StompUserPrincipal(userId.toString());
    }

    private record StompUserPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
