package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.NotificationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.notification.NotificationSearchRequest;
import com.agribank.qldvutils.request.notification.NotificationSearchQuery;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.notification.NotificationItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Inbox thông báo cá nhân (bell/danh sách phân trang) — luôn lọc theo userId của người đang đăng
 * nhập (SecurityContext), KHÔNG nhận userId từ client. Không gắn @RequirePermission theo FN/action
 * (dữ liệu cá nhân, không phải phạm vi RBAC theo chức năng) — cùng pattern EmployeeInfoController,
 * chỉ cần JWT hợp lệ.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;
    private final UserService userService;

    /** 20 thông báo gần nhất — dùng cho bell/dropdown. */
    public List<NotificationItemResponse> getRecent() {
        String userId = requireUserId();
        return safeList(notificationClient.recent(userId).getData());
    }

    public PageResponse<NotificationItemResponse> search(NotificationSearchRequest request) {
        String userId = requireUserId();

        NotificationSearchQuery query = new NotificationSearchQuery();
        query.setPage(request.getPage());
        query.setPageSize(request.getPageSize());
        query.setSort(request.getSort());
        query.setOrderBy(request.getOrderBy());
        query.setUserId(userId);

        return notificationClient.search(query).getData();
    }

    /** WHERE id + userId đã tự loại trường hợp đánh dấu thông báo của người khác ở qldv-db — 0 dòng ảnh hưởng nghĩa là không tìm thấy (kể cả do không thuộc về user này). */
    public void markSeen(String id) {
        String userId = requireUserId();
        Boolean updated = notificationClient.markSeen(id, userId).getData();
        if (!Boolean.TRUE.equals(updated)) {
            throw new NotFoundException("Không tìm thấy thông báo");
        }
    }

    private String requireUserId() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user.getId();
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
