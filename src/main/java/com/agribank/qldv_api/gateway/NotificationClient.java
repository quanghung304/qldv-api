package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.entity.Notification;
import com.agribank.qldvutils.request.notification.NotificationPersistRequest;
import com.agribank.qldvutils.request.notification.NotificationSearchQuery;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.notification.NotificationItemResponse;
import com.agribank.qldvutils.response.notification.NotificationPersistResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notificationClient", url = "${qldv.database.url}" + "/api/v1/notifications", configuration = DatabaseFeignConfiguration.class)
public interface NotificationClient extends BaseClient<Notification, String> {
    /** Ghi PMDV_NOTIFICATION + N PMDV_NOTIFICATION_RECIPIENT trong 1 transaction (qldv-db). */
    @PostMapping("/persist")
    BaseResponse<NotificationPersistResult> persist(@RequestBody NotificationPersistRequest request);

    @GetMapping("/recent")
    DefaultListResponse<NotificationItemResponse> recent(@RequestParam String userId);

    @PostMapping("/search")
    BaseResponse<PageResponse<NotificationItemResponse>> search(@RequestBody NotificationSearchQuery request);

    @PostMapping("/recipients/{id}/seen")
    BaseResponse<Boolean> markSeen(@PathVariable String id, @RequestParam String userId);
}
