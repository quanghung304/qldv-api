package com.agribank.qldv_api.workflow;


import com.agribank.qldv_api.enums.ECaseStatusCode;

import java.util.List;

import static com.agribank.qldv_api.enums.ECaseStatusCode.A_01;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_02;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_03;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_04;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_05;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_06;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_07;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_08;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_09;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_10;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_11;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_12;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_13;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_14;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_15;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_16;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_17;
import static com.agribank.qldv_api.enums.ECaseStatusCode.B_01;
import static com.agribank.qldv_api.enums.ECaseStatusCode.B_02;
import static com.agribank.qldv_api.enums.ECaseStatusCode.B_03;
import static com.agribank.qldv_api.enums.ECaseStatusCode.B_04;
import static com.agribank.qldv_api.enums.ECaseStatusCode.B_05;
import static com.agribank.qldv_api.enums.ECaseStatusCode.C_01;
import static com.agribank.qldv_api.enums.ECaseStatusCode.C_02;
import static com.agribank.qldv_api.enums.ECaseStatusCode.C_03;
import static com.agribank.qldv_api.enums.ECaseStatusCode.C_04;

/**
 * Cấu hình 34 transition của module Tổ chức Đảng — chép NGUYÊN VẸN từ mục 6
 * (Bảng cấu hình transition tổng hợp) trong 02_Workflow_StateMachine.docx v0.2. Đây là quy trình
 * nghiệp vụ Đảng đã quy định, hiếm khi đổi, và KHÔNG có giao diện quản trị — nên hardcode ở đây,
 * KHÔNG lưu CSDL. Không rải if/else — mọi guard đọc từ danh sách RULES bên dưới.
 *
 * Trạng thái nguồn/đích dùng {@link ECaseStatusCode} (mã + mô tả) thay vì String literal rời rạc
 * — vẫn so khớp với status_id (String) của PMDV_CASE qua {@code ECaseStatusCode.getCode()}.
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
            rule("A", A_01, "SUBMIT_CONTROL", A_02, "R-CV"),
            rule("A", A_02, "RETURN", A_01, "R-KS"),
            rule("A", A_02, "APPROVE_FORWARD", A_03, "R-KS"),
            rule("A", A_03, "RETURN", A_01, "R-LD"),
            rule("A", A_03, "APPROVE", A_04, "R-LD"),
            rule("A", A_04, "UPLOAD_BTV_RESULT", A_05, "R-CV"),
            rule("A", A_05, "SUBMIT_CONTROL", A_06, "R-CV"),
            rule("A", A_06, "RETURN", A_05, "R-KS"),
            rule("A", A_06, "APPROVE_FORWARD", A_07, "R-KS"),
            rule("A", A_07, "RETURN", A_05, "R-LD"),
            rule("A", A_07, "APPROVE", A_08, "R-LD"),
            rule("A", A_08, "UPDATE_BCH_MINUTES", A_09, "R-CV"),
            rule("A", A_09, "SUBMIT_CONTROL", A_10, "R-CV"),
            rule("A", A_10, "RETURN", A_09, "R-KS"),
            rule("A", A_10, "APPROVE_FORWARD", A_11, "R-KS"),
            rule("A", A_11, "RETURN", A_09, "R-LD"),
            rule("A", A_11, "APPROVE", A_12, "R-LD"),
            rule("A", A_12, "SUBMIT_CONTROL", A_13, "R-CV"),
            rule("A", A_13, "RETURN", A_12, "R-KS"),
            rule("A", A_13, "APPROVE_FORWARD", A_14, "R-KS"),
            rule("A", A_14, "RETURN", A_12, "R-LD"),
            rule("A", A_14, "APPROVE", A_15, "R-LD"),
            rule("A", A_15, "REGISTER_SIGNED_DOC", A_16, "R-CV"),
            rule("A", A_16, "APPROVE_COMPLETE", A_17, "R-LD"),

            // Luồng B — 5 transition
            rule("B", B_01, "SUBMIT_CONTROL", B_02, "R-BPTM"),
            rule("B", B_02, "RETURN", B_01, "R-KSCS"),
            rule("B", B_02, "APPROVE_FORWARD", B_03, "R-KSCS"),
            rule("B", B_03, "APPROVE_ISSUE", B_04, "R-BPTM", "R-PDCS"),
            rule("B", B_04, "APPROVE_COMPLETE", B_05, "R-PDCS"),

            // Luồng C — 5 transition
            rule("C", C_01, "SUBMIT_CONTROL", C_02, "R-BPTM"),
            rule("C", C_02, "RETURN", C_01, "R-KSCS"),
            rule("C", C_02, "APPROVE_FORWARD", C_03, "R-KSCS"),
            rule("C", C_03, "SUBMIT_TO_PARENT", C_04, "R-PDCS"),
            rule("C", C_04, "RECEIVE_ROUTE", A_01, "R-CV")
    );

    private CaseWorkflowConfig() {
    }

    private static WorkflowTransitionRule rule(String flowCode, ECaseStatusCode fromStatusCode, String actionCode,
                                                ECaseStatusCode toStatusCode, String... roleCodes) {
        return new WorkflowTransitionRule(flowCode, fromStatusCode, actionCode, toStatusCode, List.of(roleCodes));
    }
}
