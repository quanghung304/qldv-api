package com.agribank.qldv_api.enums;

import java.util.List;

public class Constants {
    public static final Integer BRANCH_CODE_HEAD_QUARTER = 1001;
    public static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final long MAX_FILE_SIZE = 4 * 1024 * 1024; //4MB
    public static final String EMAIL_DOMAIN = "@agribank.com.vn";
    public static final Integer FIRST_LV1_BRCD = 1080;
    public static final Integer HEAD_OFFICE_BRCD = 1000;
    /**
     * PMDV_CASE.origin_flow — Luồng A: BTCĐU tự khởi tạo và phê duyệt (15 trạng thái A-01…A-12,
     * A-14, A-15 — ĐÃ RÚT GỌN, không có A-13/A-16/A-17, xem workflow-states.md mục "Luồng A đã
     * rút gọn" / CaseWorkflowConfig). Dùng khi tạo hồ sơ Thành lập TCĐ cấp Agribank
     * (API-SC02-01, luôn khởi phát Luồng A) — KHÔNG suy ra từ authority_level trong service dùng
     * chung, vì cấp cơ sở (GRASSROOTS_LEVEL) có thể là Luồng B HOẶC C tuỳ kịch bản (SC-07, Sprint 5).
     */
    public static final String CASE_FLOW_BTCDU = "A";
    /** PMDV_CASE.origin_flow — Luồng B: 1 cấp kiểm soát (R-KSCS) rồi thẳng R-PDCS (xem CaseWorkflowConfig). */
    public static final String CASE_FLOW_B = "B";
    /** PMDV_CASE.origin_flow — Luồng C: giống Luồng B tới "Trình cấp thẩm quyền cơ sở", rồi gửi trình lên BTCĐU. */
    public static final String CASE_FLOW_C = "C";

    /**
     * PMDV_DOCUMENT.document_name của văn bản "Quyết định thành lập tổ chức đảng" (API-SC05-02,
     * DecisionDocumentsService) — dùng chung để tra lại đúng document đó ở API-SC06-02
     * (CaseCompleteService, map establish_decision_no/date sang PMDV_ORGANIZATION). PHẢI khớp
     * NGUYÊN VĂN giá trị đã lưu — tách hằng số dùng chung để tránh 2 nơi tự gõ lại chuỗi này lệch
     * nhau.
     */
    public static final String ESTABLISH_DECISION_DOCUMENT_NAME = "Quyết định thành lập tổ chức đảng";


    // --- Ngưỡng validate format dùng chung cho EstablishmentCaseRequest/GrassrootsEstablishmentCaseRequest/CaseChangeRequest ---
    /** Field staff_count (SC-02/SC-07) — số nguyên dương, tối đa. */
    public static final int MAX_STAFF_COUNT = 100000;
    /** Field board_decision_no (SC-02/SC-07/SC-08) — độ dài tối đa. */
    public static final int MAX_DECISION_NO_LENGTH = 100;
    /** Field board_decision_summary (SC-02/SC-07) — độ dài tối đa. */
    public static final int MAX_BOARD_DECISION_SUMMARY_LENGTH = 1000;
    /** Field proposed_organization_name (SC-02/SC-07) — độ dài tối đa. */
    public static final int MAX_ORGANIZATION_NAME_LENGTH = 250;
    /** Field political_standard_conclusion_no (SC-02/SC-07) — độ dài tối đa. */
    public static final int MAX_CONCLUSION_NO_LENGTH = 50;
    /** BR-SC02-02 — số tháng hết hiệu lực của kết luận tiêu chuẩn chính trị (SC-02/SC-07/SC-05-02). */
    public static final long POLITICAL_STANDARD_VALIDITY_MONTHS = 6;
}
