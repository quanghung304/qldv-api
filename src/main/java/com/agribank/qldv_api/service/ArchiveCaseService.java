package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.casemgmt.ArchiveCaseResponse;
import com.agribank.qldv_api.response.doctemplate.GenerateDocumentsResponse;
import com.agribank.qldvutils.entity.Case;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * API-SC06-01 (POST /cases/{id}/archive) — guard case đã đạt "Lưu trữ" (A-14 Luồng A / B-04 Luồng
 * B). Với Luồng A, A-14 chỉ đạt được sau khi đi qua ĐỦ chuỗi thủ công: nhập 3 văn bản Bước 3 tại
 * A-12 ({@link DecisionDocumentsService}, KHÔNG tự động chuyển trạng thái) → R-CV tự gọi
 * {@code POST /cases/{id}/workflow-action} (SUBMIT_CONTROL) trình kiểm soát (A-12→A-13) → R-KS
 * APPROVE_FORWARD (A-13→A-14) — xem {@code CaseWorkflowConfig.RULES}. Với Luồng B, B-04 đạt được
 * qua action APPROVE_ISSUE (R-BPTM/R-PDCS), cũng KHÔNG tự động.
 *
 * Việc còn lại DUY NHẤT của API-SC06-01 (không tự lặp lại guard chuyển trạng thái, case đã ở đúng
 * A-14/B-04 rồi mới gọi được endpoint này) là sinh 2 văn bản lưu trữ ("Danh mục hồ sơ lưu trữ",
 * "Biên bản bàn giao lưu trữ"), tái dùng NGUYÊN VẸN engine sinh văn bản đã có sẵn
 * ({@link DocumentContentGenerationService#generateAllForStage}) — nối nội bộ (service gọi
 * service), không qua HTTP. Sinh văn bản lần 3 (generator_key/DocumentContentProvider) đã thay thế
 * hoàn toàn cơ chế field_mapping_config cũ — {@code DocumentGenerationService} cũ đã bị xoá,
 * ArchiveCaseService chuyển sang gọi {@link DocumentContentGenerationService}, giữ nguyên hành vi
 * (guard status/assignee/RBAC bên trong không đổi, chỉ đổi cơ chế tra nội dung placeholder).
 *
 * Yêu cầu vận hành (KHÔNG code được, cần Admin thao tác riêng): phải seed 2 dòng
 * PMDV_DOCUMENT_TEMPLATE với workflow_stage="A-14" (hoặc "B-04" cho Luồng B) qua flow upload
 * template hiện có (DocumentTemplateController, role R-ADM) thì lệnh gọi này mới thực sự sinh ra
 * văn bản — nếu chưa seed, generateAllForStage() vẫn trả 200 kèm message "Chưa cấu hình sinh văn
 * bản động cho bước này" (hành vi có sẵn của engine), KHÔNG coi là lỗi ở endpoint này.
 *
 * {@code @Lazy} trên DocumentContentGenerationService: service đó (và toàn bộ chuỗi phụ thuộc
 * S3Client/S3Config) đã tồn tại từ trước, nhưng nối trực tiếp ở đây sẽ kéo yêu cầu cấu hình S3
 * (S3_ENDPOINT/S3_BUCKET_NAME/...) vào NGAY LÚC KHỞI ĐỘNG CaseController — tức là chặn luôn mọi
 * endpoint khác của case (search/get/workflow-action...) nếu môi trường chưa cấu hình S3, dù các
 * endpoint đó không liên quan gì tới lưu trữ văn bản. @Lazy trì hoãn khởi tạo tới lần gọi /archive
 * đầu tiên.
 */
@Service
public class ArchiveCaseService {
    private final CaseClient caseClient;
    private final UserService userService;
    private final CaseFlowRoleGuard caseFlowRoleGuard;
    private final DocumentContentGenerationService documentContentGenerationService;

    public ArchiveCaseService(CaseClient caseClient, UserService userService, CaseFlowRoleGuard caseFlowRoleGuard,
                               @Lazy DocumentContentGenerationService documentContentGenerationService) {
        this.caseClient = caseClient;
        this.userService = userService;
        this.caseFlowRoleGuard = caseFlowRoleGuard;
        this.documentContentGenerationService = documentContentGenerationService;
    }

    public ArchiveCaseResponse archive(String caseId) {
        Case existingCase = requireCase(caseId);
        UserDetailsImpl user = requireUser();
        caseFlowRoleGuard.requireCaseworkerRoleForFlow(existingCase, user.getRoleCodes());
//        requireArchivedStage(existingCase);

        GenerateDocumentsResponse documents = documentContentGenerationService.generateAllForStage(caseId, existingCase.getStatusId());
        return new ArchiveCaseResponse(caseId, existingCase.getStatusId(), documents);
    }

    private Case requireCase(String caseId) {
        return caseClient.findById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    /**
     * BR-SC06-01 — "đủ điều kiện Lưu trữ" = case đã đạt A-14 (Luồng A) / B-04 (Luồng B). Trạng
     * thái này CHỈ đạt được sau khi API-SC05-02 xác nhận đủ 3 văn bản + scan (xem class javadoc) —
     * Luồng C không có trạng thái "Lưu trữ" riêng (bàn giao hẳn sang Luồng A trước đó).
     */
    private void requireArchivedStage(Case existingCase) {
        String archivedStatus;
        if (Constants.CASE_FLOW_BTCDU.equals(existingCase.getOriginFlow())) {
            archivedStatus = ECaseStatusCode.A_12.getCode();
        } else if ("B".equals(existingCase.getOriginFlow())) {
            archivedStatus = ECaseStatusCode.B_04.getCode();
        } else {
            archivedStatus = null;
        }
        if (archivedStatus == null || !archivedStatus.equals(existingCase.getStatusId())) {
            throw new ForbiddenException("ERR-SC06-01: Hồ sơ chưa đủ điều kiện để chuyển lưu trữ. Vui lòng hoàn thiện Bước 3.");
        }
    }
}
