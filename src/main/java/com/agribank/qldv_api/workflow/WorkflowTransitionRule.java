package com.agribank.qldv_api.workflow;

import java.util.List;

/**
 * 1 dòng trong bảng cấu hình transition (mục 6, 02_Workflow_StateMachine.docx v0.2).
 * requiredRoleCodes có thể có nhiều hơn 1 giá trị (VD B-03 → APPROVE_ISSUE cho phép cả
 * R-BPTM lẫn R-PDCS) — chỉ cần actor có ÍT NHẤT 1 role khớp (OR) là đủ.
 */
public record WorkflowTransitionRule(
        String flowCode,
        String fromStatusCode,
        String actionCode,
        String toStatusCode,
        List<String> requiredRoleCodes) {
}
