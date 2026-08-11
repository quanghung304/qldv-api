package com.agribank.qldv_api.service.doctemplate;

import com.agribank.qldv_api.enums.Constants;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Từ điển AUTO-MATCH cho cơ chế merge template .docx (S2-03, schema v2) — dùng lúc UPLOAD template
 * để tự động gán resolution_type/field_path/resolver_id cho placeholder đã biết trước, KHÔNG tự
 * bịa thêm field ngoài danh sách đã xác nhận (26 field SIMPLE kế thừa từ bản v1 + 9 placeholder
 * DERIVED/EXTERNAL_LOOKUP theo đúng Phần 2.3 prompt S2-03 lần 2).
 *
 * field_path (SIMPLE) là key tra trong {@code FieldPathRegistry} ở qldv-db — CHỈ có Ý NGHĨA
 * DOCUMENTATION ở đây (qldv-api không tự resolve giá trị, chỉ auto-gán chuỗi này vào JSON, qldv-db
 * mới là nơi thẩm định + tra giá trị thật lúc sinh văn bản).
 *
 * resolver_id/resolver_params PHẢI khớp đúng bean name đã đăng ký: DERIVED tra ở qldv-db
 * (com.agribank.qldvdb.docgen.resolver.*), EXTERNAL_LOOKUP tra ở qldv-api
 * (com.agribank.qldv_api.service.doctemplate.*, VD BranchNameByBrcdResolver — dùng
 * IAMClient/BranchService, không đặt ở qldv-db) — xem danh sách resolver_id trong báo cáo cuối
 * task để đối chiếu đủ/thiếu.
 *
 * Field dạng danh sách lặp (VD danh sách cấp ủy dự kiến) KHÔNG có trong catalog này — Pha 2
 * (REPEAT_BLOCK) riêng, không xử lý ở đây.
 */
public class FieldCatalog {

    /** Cú pháp placeholder trong file .docx mẫu: [ten_field] — dùng chung cho cả bước đọc placeholder (upload) và bước merge (generate). */
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[([a-zA-Z0-9_]+)]");

    public static final String SIMPLE = "SIMPLE";
    public static final String DERIVED = "DERIVED";
    public static final String EXTERNAL_LOOKUP = "EXTERNAL_LOOKUP";

    public record Entry(String placeholderKey, String resolutionType, String fieldPath,
                         String resolverId, Map<String, String> resolverParams) {
    }

    private static Entry simple(String placeholderKey, String fieldPath) {
        return new Entry(placeholderKey, SIMPLE, fieldPath, null, null);
    }

    private static Entry derived(String placeholderKey, String resolverId, Map<String, String> params) {
        return new Entry(placeholderKey, DERIVED, null, resolverId, params);
    }

    private static Entry externalLookup(String placeholderKey, String resolverId, Map<String, String> params) {
        return new Entry(placeholderKey, EXTERNAL_LOOKUP, null, resolverId, params);
    }

    public static final List<Entry> ENTRIES = List.of(
            // SIMPLE — field_path tra FieldPathRegistry (qldv-db)
            simple("case_code", "case.caseCode"),
            simple("case_type_name", "case.caseTypeName"),
            simple("authority_level_name", "case.authorityLevelName"),
            simple("origin_flow", "case.originFlow"),
            simple("organization_name", "case.organizationName"),
            simple("organization_code", "case.organizationCode"),
            simple("organization_type_name", "case.organizationTypeName"),
            simple("brcd", "establishment.brcd"),
            simple("member_count", "establishment.memberCount"),
            simple("committee_member_count", "establishment.committeeMemberCount"),
            simple("committee_structure", "establishment.committeeStructure"),
            simple("board_decision_no", "establishment.boardDecisionNo"),
            simple("board_decision_date", "establishment.boardDecisionDate"),
            simple("board_decision_summary", "establishment.boardDecisionSummary"),
            simple("political_standard_conclusion_no", "establishment.politicalStandardConclusionNo"),
            simple("political_standard_conclusion_date", "establishment.politicalStandardConclusionDate"),
            simple("staff_count", "establishment.staffCount"),
            simple("leadership_info_text", "establishment.leadershipInfoText"),
            simple("method", "boardReview.method"),
            simple("board_document_no", "boardReview.boardDocumentNo"),
            simple("board_document_date", "boardReview.boardDocumentDate"),
            simple("ballots_issued", "boardReview.ballotsIssued"),
            simple("ballots_returned", "boardReview.ballotsReturned"),
            simple("ballots_agree", "boardReview.ballotsAgree"),
            simple("ballots_disagree", "boardReview.ballotsDisagree"),
            simple("opinion_notes", "boardReview.opinionNotes"),

            // DERIVED / EXTERNAL_LOOKUP — resolver_id tra Resolver Registry (qldv-db)
            derived("secretary_full_name", "PROPOSED_COMMITTEE_MEMBER_BY_POSITION",
                    Map.of("position", "SECRETARY", "attribute", "fullName")),
            derived("secretary_professional_title", "PROPOSED_COMMITTEE_MEMBER_BY_POSITION",
                    Map.of("position", "SECRETARY", "attribute", "professionalTitle")),
            derived("deputy_secretary_full_name", "PROPOSED_COMMITTEE_MEMBER_BY_POSITION",
                    Map.of("position", "DEPUTY_SECRETARY", "attribute", "fullName")),
            derived("deputy_secretary_professional_title", "PROPOSED_COMMITTEE_MEMBER_BY_POSITION",
                    Map.of("position", "DEPUTY_SECRETARY", "attribute", "professionalTitle")),
            derived("current_committee_member_count", "CURRENT_COMMITTEE_MEMBER_COUNT",
                    Map.of("organizationRole", "SOURCE")),
            derived("creator_full_name", "CASE_CREATOR_NAME", Map.of()),
            derived("controller_full_name", "CASE_HISTORY_ACTOR", Map.of("action", "APPROVE_FORWARD")),
            derived("approver_full_name", "CASE_HISTORY_ACTOR", Map.of("action", "APPROVE")),
            externalLookup("external_branch_name", "BRANCH_NAME_BY_BRCD", Map.of())
    );

    private static final Map<String, Entry> BY_KEY = ENTRIES.stream()
            .collect(Collectors.toMap(Entry::placeholderKey, Function.identity()));

    private FieldCatalog() {
    }

    /** Placeholder [ten_field] khớp catalog khi tên (không ngoặc vuông) trùng CHÍNH XÁC 1 key ở đây. */
    public static Entry findByKey(String key) {
        return BY_KEY.get(key);
    }
}
