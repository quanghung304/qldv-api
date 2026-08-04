package com.agribank.qldv_api.enums;

import java.util.List;

public class Constants {
    public static final String DANG_UY_AGRIBANK_CODE = "1000";
    public static final String BTCDU_CODE = "1001";
    public static final Integer BRANCH_CODE_HEAD_QUARTER = 1001;
    public static final Integer FORM_B_NAME_LENGTH = 4;
    public static final Integer FORM_C_NAME_LENGTH = 6;
    public static final String FORM_C1_NAME = "C1";
    public static final String FORM_B1_NAME = "B1";
    public static final List<Integer> ORGANIZATION_NAME_LENGTH = List.of(FORM_B_NAME_LENGTH,6,8);
    public static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final long MAX_FILE_SIZE = 4 * 1024 * 1024; //4MB
    public static final String EMAIL_DOMAIN = "@agribank.com.vn";
    public static final Integer FIRST_LV1_BRCD = 1080;
    public static final Integer HEAD_OFFICE_BRCD = 1000;
    /**
     * PMDV_CASE.origin_flow — Luồng A: BTCĐU tự khởi tạo và phê duyệt (17 trạng thái A-01…A-17,
     * xem workflow-states.md / CaseWorkflowConfig). Dùng khi tạo hồ sơ Thành lập TCĐ cấp Agribank
     * (API-SC02-01, luôn khởi phát Luồng A) — KHÔNG suy ra từ authority_level trong service dùng
     * chung, vì cấp cơ sở (GRASSROOTS_LEVEL) có thể là Luồng B HOẶC C tuỳ kịch bản (SC-07, Sprint 5).
     */
    public static final String CASE_FLOW_BTCDU = "A";
    /**
     * PMDV_DOCUMENT.document_name của văn bản "Quyết định thành lập tổ chức đảng" (API-SC05-02,
     * DecisionDocumentsService) — dùng chung để tra lại đúng document đó ở API-SC06-02
     * (CaseCompleteService, map establish_decision_no/date sang PMDV_ORGANIZATION). PHẢI khớp
     * NGUYÊN VĂN giá trị đã lưu — tách hằng số dùng chung để tránh 2 nơi tự gõ lại chuỗi này lệch
     * nhau.
     */
    public static final String ESTABLISH_DECISION_DOCUMENT_NAME = "Quyết định thành lập tổ chức đảng";
}
