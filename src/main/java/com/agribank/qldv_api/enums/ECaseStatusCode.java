package com.agribank.qldv_api.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Toàn bộ status_code của PMDV_CASE (module Tổ chức Đảng) — 26 trạng thái, 3 luồng A/B/C, chép
 * nguyên từ mục "Danh mục trạng thái" của 02_Workflow_StateMachine.docx v0.2 (Luồng A: A-01..A-17,
 * Luồng B: B-01..B-05, Luồng C: C-01..C-04). Dùng thay cho string literal rải rác trong
 * {@code CaseWorkflowConfig}/service — không đổi {@code code} (đồng bộ CSDL/API/tài liệu nghiệp
 * vụ, xem AGENTS.md mục "Ranh giới").
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ECaseStatusCode {
    // Luồng A — BTCĐU tự khởi tạo và phê duyệt (17 trạng thái)
    A_01("A-01", "Đang thực hiện (trước họp BTV)"),
    A_02("A-02", "Trình kiểm soát (trước họp BTV)"),
    A_03("A-03", "Trình lãnh đạo ban (trước họp BTV)"),
    A_04("A-04", "Trình Ban Thường vụ"),
    A_05("A-05", "Đang thực hiện (sau họp BTV)"),
    A_06("A-06", "Trình kiểm soát (sau họp BTV)"),
    A_07("A-07", "Trình lãnh đạo ban (sau họp BTV)"),
    A_08("A-08", "Trình Ban Chấp hành"),
    A_09("A-09", "Đang thực hiện (sau họp BCH)"),
    A_10("A-10", "Trình kiểm soát (sau họp BCH)"),
    A_11("A-11", "Trình lãnh đạo ban (sau họp BCH)"),
    A_12("A-12", "Đang thực hiện (ban hành QĐ)"),
    A_13("A-13", "Trình kiểm soát (ban hành QĐ)"),
    A_14("A-14", "Trình lãnh đạo ban (ban hành QĐ)"),
    A_15("A-15", "Trình ban hành QĐ"),
    A_16("A-16", "Lưu trữ"),
    A_17("A-17", "Hoàn thành"),

    // Luồng B — Đảng bộ cơ sở tự khởi tạo và phê duyệt (5 trạng thái)
    B_01("B-01", "Đang thực hiện"),
    B_02("B-02", "Trình kiểm soát"),
    B_03("B-03", "Trình BCH"),
    B_04("B-04", "Lưu trữ"),
    B_05("B-05", "Hoàn thành"),

    // Luồng C — Đảng bộ cơ sở khởi tạo, trình lên BTCĐU (4 trạng thái)
    C_01("C-01", "Đang thực hiện"),
    C_02("C-02", "Trình kiểm soát"),
    C_03("C-03", "Trình cấp thẩm quyền cơ sở"),
    C_04("C-04", "Trình BTCĐU");

    String code;
    String description;

    public static ECaseStatusCode fromCode(String code) {
        for (ECaseStatusCode value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy status_code: " + code);
    }
}
