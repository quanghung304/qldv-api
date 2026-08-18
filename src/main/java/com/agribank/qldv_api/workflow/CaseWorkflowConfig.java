package com.agribank.qldv_api.workflow;


import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.enums.ERoleCode;

import java.util.List;
import java.util.stream.Stream;

import static com.agribank.qldv_api.enums.ECaseStatusCode.A_01;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_02;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_03;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_04;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_06;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_07;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_08;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_10;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_11;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_12;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_13;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_14;
import static com.agribank.qldv_api.enums.ECaseStatusCode.A_15;
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
 * Cấu hình 29 transition của module Tổ chức Đảng — Luồng B/C chép NGUYÊN VẸN từ mục 6
 * (Bảng cấu hình transition tổng hợp) trong 02_Workflow_StateMachine.docx v0.2; Luồng A đã rút
 * gọn 1 phần so với bản gốc (xem javadoc {@link ECaseStatusCode}). Đây là quy trình nghiệp vụ
 * Đảng đã quy định, hiếm khi đổi, và KHÔNG có giao diện quản trị — nên hardcode ở đây, KHÔNG lưu
 * CSDL. Không rải if/else — mọi guard đọc từ danh sách RULES bên dưới.
 *
 * Trạng thái nguồn/đích dùng {@link ECaseStatusCode} (mã + mô tả) thay vì String literal rời rạc
 * — vẫn so khớp với status_id (String) của PMDV_CASE qua {@code ECaseStatusCode.getCode()}.
 *
 * Luồng A: 19 transition (A-01…A-15, ĐÃ RÚT GỌN MỘT PHẦN — xem javadoc {@link ECaseStatusCode}).
 * Cụm "ban hành QĐ" (từ A-12) CHỈ còn 1 cấp kiểm soát (A-13, R-KS), KHÔNG còn cấp "lãnh đạo ban"
 * như 3 cụm trước — Bước 4 (decision-documents) nhập tại A-12 (KHÔNG tự động chuyển trạng thái
 * nữa, xem DecisionDocumentsService), R-CV tự SUBMIT_CONTROL trình kiểm soát (A-13), R-KS
 * APPROVE_FORWARD thẳng sang "Lưu trữ" (A-14), rồi APPROVE_COMPLETE (R-LD) → A-15 ("Hoàn thành").
 * Luồng B: 5 transition (B-01…B-05) — 1 cấp kiểm soát (R-KSCS) rồi thẳng R-PDCS.
 * Luồng C: 5 transition (C-01…C-04) — giai đoạn 1 giống Luồng B tới "Trình cấp thẩm quyền cơ
 * sở", R-PDCS gửi trình lên BTCĐU (SUBMIT_TO_PARENT) thay vì tự ban hành; RECEIVE_ROUTE bàn giao
 * sang Luồng A tại A-01 (nguyên vẹn, không rút gọn).
 *
 * RECEIVE_ROUTE (C-04 → A-01): ĐÃ CHỐT — khi kích hoạt, status_id đổi sang A-01 nhưng
 * origin_flow của hồ sơ GIỮ NGUYÊN 'C' (không đổi sang 'A'). Engine không tự đổi origin_flow ở
 * bất kỳ transition nào trong 29 dòng này — mặc định luôn giữ nguyên.
 */
public final class CaseWorkflowConfig {

    public static final List<WorkflowTransitionRule> RULES = List.of(
            // Luồng A — 19 transition
            rule("A", A_01, ECaseWorkflowAction.SUBMIT_CONTROL, A_02, ERoleCode.R_CV),
            rule("A", A_02, ECaseWorkflowAction.RETURN, A_01, ERoleCode.R_KS),
            rule("A", A_02, ECaseWorkflowAction.APPROVE_FORWARD, A_03, ERoleCode.R_KS),
            rule("A", A_03, ECaseWorkflowAction.RETURN, A_01, ERoleCode.R_LD),
            rule("A", A_03, ECaseWorkflowAction.APPROVE, A_04, ERoleCode.R_LD),
            rule("A", A_04, ECaseWorkflowAction.SUBMIT_CONTROL, A_06, ERoleCode.R_CV),
            rule("A", A_06, ECaseWorkflowAction.RETURN, A_04, ERoleCode.R_KS),
            rule("A", A_06, ECaseWorkflowAction.APPROVE_FORWARD, A_07, ERoleCode.R_KS),
            rule("A", A_07, ECaseWorkflowAction.RETURN, A_04, ERoleCode.R_LD),
            rule("A", A_07, ECaseWorkflowAction.APPROVE, A_08, ERoleCode.R_LD),
            rule("A", A_08, ECaseWorkflowAction.SUBMIT_CONTROL, A_10, ERoleCode.R_CV),
            rule("A", A_10, ECaseWorkflowAction.RETURN, A_08, ERoleCode.R_KS),
            rule("A", A_10, ECaseWorkflowAction.APPROVE_FORWARD, A_11, ERoleCode.R_KS),
            rule("A", A_11, ECaseWorkflowAction.RETURN, A_08, ERoleCode.R_LD),
            rule("A", A_11, ECaseWorkflowAction.APPROVE, A_12, ERoleCode.R_LD),
            // Cụm "ban hành QĐ" — CHỈ 1 cấp kiểm soát (A-13), KHÔNG có cấp "lãnh đạo ban" như 3
            // cụm trên. Bước 4 (decision-documents) nhập tại A-12 KHÔNG tự động chuyển trạng thái
            // — R-CV phải tự gọi SUBMIT_CONTROL (qua endpoint dùng chung) sau khi nhập đủ dữ liệu.
            rule("A", A_12, ECaseWorkflowAction.SUBMIT_CONTROL, A_13, ERoleCode.R_CV),
            rule("A", A_13, ECaseWorkflowAction.RETURN, A_12, ERoleCode.R_KS),
            rule("A", A_13, ECaseWorkflowAction.APPROVE_FORWARD, A_14, ERoleCode.R_KS),
            rule("A", A_14, ECaseWorkflowAction.APPROVE_COMPLETE, A_15, ERoleCode.R_LD),

            // Luồng B — 5 transition
            rule("B", B_01, ECaseWorkflowAction.SUBMIT_CONTROL, B_02, ERoleCode.R_BPTM),
            rule("B", B_02, ECaseWorkflowAction.RETURN, B_01, ERoleCode.R_KSCS),
            rule("B", B_02, ECaseWorkflowAction.APPROVE_FORWARD, B_03, ERoleCode.R_KSCS),
            rule("B", B_03, ECaseWorkflowAction.APPROVE_ISSUE, B_04, ERoleCode.R_BPTM, ERoleCode.R_PDCS),
            rule("B", B_04, ECaseWorkflowAction.APPROVE_COMPLETE, B_05, ERoleCode.R_PDCS),

            // Luồng C — 5 transition
            rule("C", C_01, ECaseWorkflowAction.SUBMIT_CONTROL, C_02, ERoleCode.R_BPTM),
            rule("C", C_02, ECaseWorkflowAction.RETURN, C_01, ERoleCode.R_KSCS),
            rule("C", C_02, ECaseWorkflowAction.APPROVE_FORWARD, C_03, ERoleCode.R_KSCS),
            rule("C", C_03, ECaseWorkflowAction.SUBMIT_TO_PARENT, C_04, ERoleCode.R_PDCS),
            rule("C", C_04, ECaseWorkflowAction.RECEIVE_ROUTE, A_01, ERoleCode.R_CV)
    );

    private CaseWorkflowConfig() {
    }

    private static WorkflowTransitionRule rule(String flowCode, ECaseStatusCode fromStatusCode,
                                                ECaseWorkflowAction action,
                                                ECaseStatusCode toStatusCode, ERoleCode... roles) {
        List<String> roleCodes = Stream.of(roles).map(ERoleCode::getCode).toList();
        return new WorkflowTransitionRule(flowCode, fromStatusCode, action.name(), toStatusCode, roleCodes);
    }
}
