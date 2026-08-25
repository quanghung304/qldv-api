package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.notification.NotificationSearchRequest;
import com.agribank.qldv_api.service.NotificationService;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.notification.NotificationItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inbox thông báo cá nhân của user đang đăng nhập — xem NotificationService. */
@RestController
@RequestMapping(value = "/api/v1/notifications", produces = "application/json")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /** 20 thông báo gần nhất — dùng cho bell/dropdown. */
    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<List<NotificationItemResponse>>> recent() {
        return BaseResponse.success(notificationService.getRecent());
    }

    /** Danh sách thông báo phân trang đầy đủ. */
    @PostMapping
    public ResponseEntity<BaseResponse<PageResponse<NotificationItemResponse>>> search(
            @RequestBody NotificationSearchRequest request) {
        return BaseResponse.success(notificationService.search(request));
    }

    @PostMapping("/{id}/seen")
    public ResponseEntity<BaseResponse<String>> markSeen(@PathVariable String id) {
        notificationService.markSeen(id);
        return BaseResponse.success("Success");
    }
}
