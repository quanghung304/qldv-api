package com.agribank.qldv_api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket (SockJS fallback) — dùng để đẩy PMDV_NOTIFICATION real-time cho đúng user
 * đang online, thay vì bắt FE polling GET /api/v1/notifications/recent. Client subscribe
 * {@code /user/queue/notifications}; server đẩy qua {@code NotificationPushService} (dùng
 * {@code SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", payload)}).
 *
 * Auth xem {@link WebSocketAuthHandshakeInterceptor} — endpoint {@code /ws} KHÔNG nằm trong danh
 * sách permitAll của {@code SecurityConfiguration} nên vẫn qua {@code JwtTokenFilter} (không ảnh
 * hưởng gì, filter đó chỉ set SecurityContext cho request HTTP handshake, WS/STOMP tự xác thực
 * riêng ở đây vì trình duyệt không cho set header Authorization trên WebSocket).
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthHandshakeInterceptor authHandshakeInterceptor;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UserPrincipalHandshakeHandler())
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
