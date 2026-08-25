package com.agribank.qldv_api.service;

import com.agribank.qldvutils.response.notification.NotificationItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Đẩy PMDV_NOTIFICATION real-time qua WebSocket/STOMP (xem WebSocketConfig) tới đúng user đang
 * online — user KHÔNG online (chưa kết nối /ws) thì convertAndSendToUser() no-op (không lỗi,
 * không cần kiểm tra online trước), user vẫn thấy thông báo qua GET /recent lần sau. Lỗi ở đây
 * KHÔNG được làm hỏng luồng chính đã persist xong — chỉ log warn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushService {
    private static final String DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void push(String userId, NotificationItemResponse payload) {
        try {
            messagingTemplate.convertAndSendToUser(userId, DESTINATION, payload);
        } catch (Exception e) {
            log.warn("Đẩy WebSocket thông báo thất bại (userId {}, notificationRecipientId {}): {}",
                    userId, payload.getId(), e.getMessage(), e);
        }
    }
}
