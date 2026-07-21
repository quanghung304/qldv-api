package com.agribank.qldv_api.workflow;

import java.util.List;

/**
 * Cấu hình 34 transition của module Tổ chức Đảng — chép NGUYÊN VẸN từ mục 6
 * (Bảng cấu hình transition tổng hợp) trong 02_Workflow_StateMachine.docx v0.2. Đây là quy trình
 * nghiệp vụ Đảng đã quy định, hiếm khi đổi, và KHÔNG có giao diện quản trị — nên hardcode ở đây,
 * KHÔNG lưu CSDL. Không rải if/else — mọi guard đọc từ danh sách RULES bên dưới.
 *
 * Luồng A: 24 transition (A-01…A-17) — khuôn mẫu đầy đủ nhất (2 cấp kiểm soát: R-KS rồi R-LD).
 * Luồng B: 5 transition (B-01…B-05) — 1 cấp kiểm soát (R-KSCS) rồi thẳng R-PDCS.
 * Luồng C: 5 transition (C-01…C-04) — giai đoạn 1 giống Luồng B tới "Trình cấp thẩm quyền cơ
 * sở", R-PDCS gửi trình lên BTCĐU (SUBMIT_TO_PARENT) thay vì tự ban hành; RECEIVE_ROUTE bàn giao
 * sang Luồng A tại A-01 (nguyên vẹn, không rút gọn).
 *
 * RECEIVE_ROUTE (C-04 → A-01): ĐÃ CHỐT — khi kích hoạt, status_id đổi sang A-01 nhưng
 * origin_flow của hồ sơ GIỮ NGUYÊN 'C' (không đổi sang 'A'). Engine không tự đổi origin_flow ở
 * bất kỳ transition nào trong 34 dòng này — mặc định luôn giữ nguyên.
 */
public final class CaseWorkflowConfig {

    public static final List<WorkflowTransitionRule> RULES = List.of(
            // Luồng A — 24 transition
            rule("A", "A-01", "SUBMIT_CONTROL", "A-02", "R-CV"),
            rule("A", "A-02", "RETURN", "A-01", "R-KS"),
            rule("A", "A-02", "APPROVE_FORWARD", "A-03", "R-KS"),
            rule("A", "A-03", "RETURN", "A-01", "R-LD"),
            rule("A", "A-03", "APPROVE", "A-04", "R-LD"),
            rule("A", "A-04", "UPLOAD_BTV_RESULT", "A-05", "R-CV"),
            rule("A", "A-05", "SUBMIT_CONTROL", "A-06", "R-CV"),
            rule("A", "A-06", "RETURN", "A-05", "R-KS"),
            rule("A", "A-06", "APPROVE_FORWARD", "A-07", "R-KS"),
            rule("A", "A-07", "RETURN", "A-05", "R-LD"),
            rule("A", "A-07", "APPROVE", "A-08", "R-LD"),
            rule("A", "A-08", "UPDATE_BCH_MINUTES", "A-09", "R-CV"),
            rule("A", "A-09", "SUBMIT_CONTROL", "A-10", "R-CV"),
            rule("A", "A-10", "RETURN", "A-09", "R-KS"),
            rule("A", "A-10", "APPROVE_FORWARD", "A-11", "R-KS"),
            rule("A", "A-11", "RETURN", "A-09", "R-LD"),
            rule("A", "A-11", "APPROVE", "A-12", "R-LD"),
            rule("A", "A-12", "SUBMIT_CONTROL", "A-13", "R-CV"),
            rule("A", "A-13", "RETURN", "A-12", "R-KS"),
            rule("A", "A-13", "APPROVE_FORWARD", "A-14", "R-KS"),
            rule("A", "A-14", "RETURN", "A-12", "R-LD"),
            rule("A", "A-14", "APPROVE", "A-15", "R-LD"),
            rule("A", "A-15", "REGISTER_SIGNED_DOC", "A-16", "R-CV"),
            rule("A", "A-16", "APPROVE_COMPLETE", "A-17", "R-LD"),

            // Luồng B — 5 transition
            rule("B", "B-01", "SUBMIT_CONTROL", "B-02", "R-BPTM"),
            rule("B", "B-02", "RETURN", "B-01", "R-KSCS"),
            rule("B", "B-02", "APPROVE_FORWARD", "B-03", "R-KSCS"),
            rule("B", "B-03", "APPROVE_ISSUE", "B-04", "R-BPTM", "R-PDCS"),
            rule("B", "B-04", "APPROVE_COMPLETE", "B-05", "R-PDCS"),

            // Luồng C — 5 transition
            rule("C", "C-01", "SUBMIT_CONTROL", "C-02", "R-BPTM"),
            rule("C", "C-02", "RETURN", "C-01", "R-KSCS"),
            rule("C", "C-02", "APPROVE_FORWARD", "C-03", "R-KSCS"),
            rule("C", "C-03", "SUBMIT_TO_PARENT", "C-04", "R-PDCS"),
            rule("C", "C-04", "RECEIVE_ROUTE", "A-01", "R-CV")
    );

    private CaseWorkflowConfig() {
    }

    private static WorkflowTransitionRule rule(String flowCode, String fromStatusCode, String actionCode,
                                                String toStatusCode, String... roleCodes) {
        return new WorkflowTransitionRule(flowCode, fromStatusCode, actionCode, toStatusCode, List.of(roleCodes));
    }
}
