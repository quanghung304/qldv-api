package com.agribank.qldv_api.workflow;

import com.agribank.qldv_api.enums.ECaseStatusCode;

import java.util.List;

/**
 * 1 dòng trong bảng cấu hình transition (mục 6, 02_Workflow_StateMachine.docx v0.2).
 * requiredRoleCodes có thể có nhiều hơn 1 giá trị (VD B-03 → APPROVE_ISSUE cho phép cả
 * R-BPTM lẫn R-PDCS) — chỉ cần actor có ÍT NHẤT 1 role khớp (OR) là đủ.

 * fromStatusCode/toStatusCode dùng {@link ECaseStatusCode} (enum mã + mô tả) thay vì String rời
 * rạc — vẫn so khớp với status_id (String) lưu ở PMDV_CASE qua {@code ECaseStatusCode.getCode()}.
 */
public record WorkflowTransitionRule(
        String flowCode,
        ECaseStatusCode fromStatusCode,
        String actionCode,
        ECaseStatusCode toStatusCode,
        List<String> requiredRoleCodes) {
}
