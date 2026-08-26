package com.agribank.qldv_api.workflow;

import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.gateway.CaseBoardReviewClient;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseBoardReview;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Tra "điều kiện họp/không họp" của 1 workflowStage — dùng để lọc {@code PMDV_DOCUMENT_TEMPLATE
 * .condition_key} khi liệt kê/sinh văn bản (thay Resolver Registry cũ theo từng field). Hardcode
 * trong code, KHÔNG lưu DB — cùng tinh thần 29-transition hardcode ở {@link CaseWorkflowConfig}.
 *
 * <p><b>GIẢ ĐỊNH CẦN HÙNG XÁC NHẬN</b> — {@code docs/domain/workflow-states.md} chỉ liệt kê tổng số
 * trạng thái/transition từng luồng, KHÔNG có bảng nhóm "Bước 1/2/3 gồm những status_code nào" ở mức
 * chi tiết cần cho hàm này (và bản thân file đó đang ghi "17 trạng thái A-01…A-17" cho Luồng A —
 * ĐÃ CŨ, không khớp {@link ECaseStatusCode} hiện tại chỉ còn 15 trạng thái A-01..A-15). Suy luận
 * dưới đây dựa trên mô tả tiếng Việt từng status_code trong chính {@link ECaseStatusCode} và trình
 * tự SUBMIT_CONTROL/RETURN/APPROVE_FORWARD/APPROVE của {@link CaseWorkflowConfig}, KHÔNG suy đoán
 * thêm ngoài đó:
 * <ul>
 *   <li>"Bước 1 hoặc Bước 2" (dùng {@code case.btv_method} — hình thức DỰ KIẾN) = cụm A-01 (Bước 1,
 *   "Khởi tạo hồ sơ") + A-02/A-03 (kiểm soát/lãnh đạo TRƯỚC họp BTV, tức đang duyệt nội dung Bước 1)
 *   + A-04 (Bước 2, "Trình Ban Thường vụ" — thời điểm ghi nhận ý kiến BTV, board review có thể CHƯA
 *   chốt xong).</li>
 *   <li>"Bước 3" (dùng {@code PMDV_CASE_BOARD_REVIEW.method} — hình thức THỰC TẾ đã diễn ra) = cụm
 *   A-06/A-07 (kiểm soát/lãnh đạo TRƯỚC họp BCH, xảy ra SAU khi A-04 đã SUBMIT_CONTROL nên board
 *   review chắc chắn đã chốt) + A-08 (Bước 3, "Trình Ban Chấp hành").</li>
 *   <li>Mọi workflowStage khác (A-10 trở đi, toàn bộ Luồng B/C) -> {@code null}, đúng tinh thần
 *   "bước không có khái niệm họp/không họp".</li>
 * </ul>
 * Nếu suy luận trên sai, chỉ cần sửa lại 2 tập hằng số {@link #BTV_METHOD_STAGES}/
 * {@link #BOARD_REVIEW_METHOD_STAGES} bên dưới — không ảnh hưởng chỗ khác.
 */
@Component
@RequiredArgsConstructor
public class WorkflowConditionResolver {
    private static final Set<String> BTV_METHOD_STAGES = Set.of(
            ECaseStatusCode.A_01.getCode(), ECaseStatusCode.A_02.getCode(),
            ECaseStatusCode.A_03.getCode(), ECaseStatusCode.A_04.getCode());
    private static final Set<String> BOARD_REVIEW_METHOD_STAGES = Set.of(
            ECaseStatusCode.A_06.getCode(), ECaseStatusCode.A_07.getCode(), ECaseStatusCode.A_08.getCode());

    private final CaseBoardReviewClient caseBoardReviewClient;

    public String resolveConditionValue(Case caseEntity, String workflowStage) {
        if (BTV_METHOD_STAGES.contains(workflowStage)) {
            return mapMethod(caseEntity.getBtvMethod());
        }
        if (BOARD_REVIEW_METHOD_STAGES.contains(workflowStage)) {
            CaseBoardReview boardReview = caseBoardReviewClient.findByCaseId(caseEntity.getId()).getData().orElse(null);
            return boardReview == null ? null : mapMethod(boardReview.getMethod());
        }
        return null;
    }

    private static String mapMethod(Integer method) {
        if (method == null) {
            return null;
        }
        for (EBoardReviewMethod value : EBoardReviewMethod.values()) {
            if (value.matches(method)) {
                return value.name();
            }
        }
        return null;
    }
}
