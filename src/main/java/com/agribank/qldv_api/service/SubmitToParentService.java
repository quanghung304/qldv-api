package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.AttachmentClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.workflow.WorkflowEngine;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BR-SC07-03 (POST /cases/{id}/submit-to-parent) — R-PDCS gửi trình hồ sơ Luồng C lên BTCĐU
 * (transition SUBMIT_TO_PARENT, C-03 → C-04). ĐẶC BIỆT action, KHÔNG đi qua
 * {@code POST /cases/{id}/workflow-action} dùng chung (đúng thiết kế ghi trong
 * {@link WorkflowEngine} javadoc) — endpoint riêng này tự kiểm tra điều kiện của riêng bước này
 * (đã có >= 1 tài liệu đính kèm) TRƯỚC KHI gọi thẳng {@code WorkflowEngine.transition(...)}.
 * status/role hiện tại có đúng bước C-03 + role R-PDCS hay không do CHÍNH
 * {@code WorkflowEngine.transition()} tự đối chiếu {@code CaseWorkflowConfig.RULES} — KHÔNG lặp
 * lại guard đó ở đây.
 */
@Service
@RequiredArgsConstructor
public class SubmitToParentService {
    private final CaseClient caseClient;
    private final AttachmentClient attachmentClient;
    private final WorkflowEngine workflowEngine;
    private final UserService userService;

    public void submitToParent(String caseId) {
        requireCase(caseId);
        UserDetailsImpl user = requireUser();
        requireAtLeastOneAttachment(caseId);

        workflowEngine.transition(caseId, ECaseWorkflowAction.SUBMIT_TO_PARENT.name(),
                user.getRoleCodes(), user.getId(), null, null);
    }

    private void requireCase(String caseId) {
        caseClient.findById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    /** BR-SC07-03 — chưa có tài liệu đính kèm nào (bất kỳ bước nào của hồ sơ) thì chặn trình duyệt điện tử lên cấp trên. */
    private void requireAtLeastOneAttachment(String caseId) {
        boolean hasAttachment = !safeList(attachmentClient.findSummaryByCaseId(caseId, null).getData()).isEmpty();
        if (!hasAttachment) {
            throw new ForbiddenException("Hồ sơ chưa có tài liệu đính kèm (Hồ sơ văn bản đề nghị), "
                    + "không thể trình duyệt điện tử lên cấp trên");
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
