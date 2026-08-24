package com.agribank.qldv_api.workflow;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tra cứu "role nào cần xử lý TIẾP THEO" từ ĐÚNG {@link CaseWorkflowConfig#RULES} đã có sẵn —
 * KHÔNG định nghĩa lại bảng transition, chỉ đọc lại. Dùng chung cho:
 * - API-EligibleAssignees (GET /cases/{caseId}/eligible-assignees): resolve role rồi tìm user
 *   đủ điều kiện được gán.
 * - {@link WorkflowEngine}: sau khi transition thành công, resolve role của bước MỚI để tự động
 *   gán assignedUserId (trừ 2 action SUBMIT_CONTROL/APPROVE_FORWARD — nhận thẳng từ client).
 *
 * {@code action == null}: bỏ qua bước tra "currentStatus + action -> targetStatus", coi
 * {@code currentStatus} CHÍNH LÀ trạng thái đích cần tìm role kế tiếp (dùng khi đã biết sẵn trạng
 * thái sau transition, xem WorkflowEngine).
 *
 * Trạng thái ĐÍCH không còn transition nào xuất phát từ đó (FINAL, VD "Hoàn thành") -> trả về
 * null, nghĩa là không còn ai cần gán tiếp. Nguyên tắc đã xác nhận: mỗi transition có ĐÚNG 1 role
 * thực hiện, và trong bảng RULES hiện tại, mọi transition xuất phát từ CÙNG 1 trạng thái đều dùng
 * CHUNG 1 role — nên "role kế tiếp" luôn xác định duy nhất khi tồn tại.
 */
@Component
public class WorkflowRoleResolver {

    public String resolveRequiredRoleForNextActor(String currentStatus, String action) {
        String targetStatus;
        if (action == null) {
            targetStatus = currentStatus;
        } else {
            WorkflowTransitionRule matchedRule = CaseWorkflowConfig.RULES.stream()
                    .filter(r -> r.fromStatusCode().getCode().equals(currentStatus) && r.actionCode().equals(action))
                    .findFirst()
                    .orElse(null);
            if (matchedRule == null) {
                return null;
            }
            targetStatus = matchedRule.toStatusCode().getCode();
        }

        List<String> outgoingRoles = CaseWorkflowConfig.RULES.stream()
                .filter(r -> r.fromStatusCode().getCode().equals(targetStatus))
                .flatMap(r -> r.requiredRoleCodes().stream())
                .distinct()
                .toList();

        return outgoingRoles.isEmpty() ? null : outgoingRoles.get(0);
    }
}
