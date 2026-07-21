package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Body của POST /cases/{id}/workflow-action — CHỈ 4 action dùng chung (xem ECaseWorkflowAction).
 * KHÔNG còn field reauth_token (đã bỏ yêu cầu xác thực lại) — @JsonIgnoreProperties để field này
 * (nếu client cũ vẫn gửi lên) bị bỏ qua, không lỗi.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkflowActionRequest {
    String action;
    String comment;

    public void validate() {
        try {
            ECaseWorkflowAction.valueOf(action);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("action phải là 1 trong: SUBMIT_CONTROL, RETURN, APPROVE_FORWARD, APPROVE");
        }
    }
}
