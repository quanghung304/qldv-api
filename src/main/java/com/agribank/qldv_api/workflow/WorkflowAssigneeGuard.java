package com.agribank.qldv_api.workflow;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Guard BỔ SUNG (không thay thế guard vai trò/trạng thái đã có) — kiểm tra người đang thao tác có
 * đúng là {@code case.assignedUserId} hay không. CHỈ áp dụng cho API GHI/chuyển trạng thái, TUYỆT
 * ĐỐI KHÔNG áp dụng cho API ĐỌC (GET /cases, GET /cases/{id}, mọi API FN4/FN7).
 *
 * {@code assignedUserId == null} (hồ sơ vừa RECEIVE_ROUTE handoff, hoặc hồ sơ cũ trước khi tính
 * năng này tồn tại) -> CHO QUA, coi là "chưa từng được gán" — không phải lỗi.
 *
 * Dùng lại ở: {@link WorkflowEngine} (mọi transition), và các API ghi khác đang ở đúng bước cho
 * thao tác đó (PUT establishment, PUT establishment/board-review, sinh văn bản, đính kèm tài liệu).
 */
@Component
@RequiredArgsConstructor
public class WorkflowAssigneeGuard {
    private final UserClient userClient;

    public void requireAssignee(Case caseEntity, String currentUserId) {
        String assignedUserId = caseEntity.getAssignedUserId();
        if (assignedUserId == null || assignedUserId.equals(currentUserId)) {
            return;
        }
        String assignedUserName = userClient.findById(assignedUserId).getData()
                .map(User::getFullName)
                .orElse(assignedUserId);
        throw new ForbiddenException("Hồ sơ này đang được giao cho " + assignedUserName
                + " xử lý, bạn không có quyền thao tác");
    }
}
