package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseHistoryClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.casemgmt.CaseDeleteResponse;
import com.agribank.qldvutils.entity.AuditLog;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.enums.ERoleCode;
import com.agribank.qldvutils.request.casemgmt.CaseDeletePersistRequest;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * API-GL-01 (DELETE /cases/{id}, prompt_S4-03) — xóa hồ sơ CHỈ khi còn ở trạng thái "Đang thực
 * hiện" LẦN ĐẦU TIÊN (chưa từng Trình kiểm soát). Áp dụng NGUYÊN VẸN cho cả case Thành lập TCĐ
 * (Luồng A) lẫn case biến động (Luồng B/C, S4-01) — A-01/B-01/C-01 dùng chung 1 bảng trạng thái
 * (GC-03, Workflow SM), KHÔNG rẽ nhánh theo case_type.
 *
 * Guard đúng thứ tự mục 3, prompt_S4-03 (RBAC "có quyền Xóa ở FN1 hay không" đã được
 * {@code @RequirePermission(function="FN1", action="DELETE")} chặn ở tầng interceptor TRƯỚC khi
 * vào tới đây — xem PermissionInterceptor — nên ở service chỉ còn lại phần business rule mà
 * interceptor không biết: role có khớp ĐÚNG luồng của case này hay không, status có đúng "giai
 * đoạn đầu tiên" hay không, và lịch sử xử lý có thật sự rỗng hay không):
 * 1. Case tồn tại (404).
 * 2-3. Role khớp origin_flow (R-CV ↔ luồng A, R-BPTM ↔ luồng B/C) — sai luồng → 403.
 * 4. status_id đúng "giai đoạn đầu tiên" của luồng đó (A-01/B-01/C-01) — sai → 403.
 * 5. PMDV_CASE_HISTORY rỗng (chưa từng qua transition nào, kể cả bị RETURN về lại A-01) — có → 403.
 *
 * [GC-S4-03-01] Đề bài còn 2 đề xuất mâu thuẫn (hard-delete theo Permission Matrix vs soft-delete
 * theo Data Model ERD), cả 2 đều tự nhận "cần Ban TCĐU xác nhận". Code theo Mục A (hard-delete)
 * — đúng tên task S4-03 hiện tại — nhưng toàn bộ xóa dữ liệu tách riêng vào
 * {@link CaseDeleteService} (qldv-db) để dễ đổi sang soft-delete sau này nếu có quyết định khác.
 *
 * [GC-S4-03-03], [GC-S4-03-04] Giới hạn "chỉ được xóa hồ sơ do chính mình tạo" và kiểm tra
 * PMDV_USER_SCOPE cho R-BPTM đều là ĐỀ XUẤT chưa được API-GL-01 xác nhận (mục 6, prompt) — KHÔNG
 * áp dụng ở đây để tránh thêm rule chưa có căn cứ; cần xác nhận với team trước khi bổ sung.
 */
@Service
@RequiredArgsConstructor
public class CaseDeleteService {
    private static final Set<String> ALLOWED_ROLES_FLOW_A = Set.of(ERoleCode.R_CV.getCode());
    private static final Set<String> ALLOWED_ROLES_FLOW_BC = Set.of(ERoleCode.R_BPTM.getCode());

    private final CaseClient caseClient;
    private final CaseHistoryClient caseHistoryClient;
    private final UserService userService;

    public CaseDeleteResponse delete(String caseId) {
        Case existingCase = requireCase(caseId);
        UserDetailsImpl user = requireUser();

        requireRoleMatchesFlow(existingCase, user.getRoleCodes());
        requireInitialStatus(existingCase);
        requireNeverSubmitted(caseId);

        AuditLog auditLog = buildAuditLog(existingCase, user.getId());
        caseClient.deleteCascade(buildPersistRequest(caseId, auditLog));

        return new CaseDeleteResponse(caseId, true);
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

    /** Mục 3, bước 2-3 — R-CV chỉ xóa được case Luồng A; R-BPTM chỉ xóa được case Luồng B/C. */
    private void requireRoleMatchesFlow(Case existingCase, List<String> roleCodes) {
        Set<String> roles = roleCodes == null ? Set.of() : new HashSet<>(roleCodes);
        boolean isFlowA = Constants.CASE_FLOW_BTCDU.equals(existingCase.getOriginFlow());
        Set<String> allowedRoles = isFlowA ? ALLOWED_ROLES_FLOW_A : ALLOWED_ROLES_FLOW_BC;
        boolean allowed = roles.stream().anyMatch(allowedRoles::contains);
        if (!allowed) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền xóa hồ sơ thuộc luồng này");
        }
    }

    /** Mục 3, bước 4 — status_id phải đúng "giai đoạn đầu tiên" của đúng luồng (A-01/B-01/C-01). */
    private void requireInitialStatus(Case existingCase) {
        String originFlow = existingCase.getOriginFlow();
        String requiredStatus;
        if (Constants.CASE_FLOW_BTCDU.equals(originFlow)) {
            requiredStatus = ECaseStatusCode.A_01.getCode();
        } else if (Constants.CASE_FLOW_B.equals(originFlow)) {
            requiredStatus = ECaseStatusCode.B_01.getCode();
        } else if (Constants.CASE_FLOW_C.equals(originFlow)) {
            requiredStatus = ECaseStatusCode.C_01.getCode();
        } else {
            requiredStatus = null;
        }
        if (requiredStatus == null || !requiredStatus.equals(existingCase.getStatusId())) {
            throw new ForbiddenException("ERR-GL-02: Hồ sơ đã qua bước Trình kiểm soát, không thể xóa");
        }
    }

    /**
     * Mục 3, bước 5 — dùng PMDV_CASE_HISTORY RỖNG thay vì chỉ dựa vào status_id, vì hồ sơ có thể
     * bị RETURN từ A-02 về lại A-01 (status_id lại = A-01 nhưng ĐÃ từng qua kiểm soát).
     */
    private void requireNeverSubmitted(String caseId) {
        List<CaseHistoryItemResponse> history = safeList(caseHistoryClient.findByCaseId(caseId).getData());
        if (!history.isEmpty()) {
            throw new ForbiddenException("ERR-GL-02: Hồ sơ đã từng qua bước Trình kiểm soát, không thể xóa");
        }
    }

    /** BR-GL-05 — bằng chứng DUY NHẤT còn sót lại rằng hồ sơ từng tồn tại (hard-delete, Mục A). */
    private AuditLog buildAuditLog(Case existingCase, String performedBy) {
        return AuditLog.builder()
                .entityName(com.agribank.qldvutils.enums.Constants.ENTITY_TABLE_CASE)
                .entityId(existingCase.getId())
                .action(EApiLogType.DELETE.name())
                .changeDetail(toJson(existingCase))
                .performedBy(performedBy)
                .performedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    private CaseDeletePersistRequest buildPersistRequest(String caseId, AuditLog auditLog) {
        CaseDeletePersistRequest request = new CaseDeletePersistRequest();
        request.setCaseId(caseId);
        request.setAuditLog(auditLog);
        return request;
    }

    private static String toJson(Case existingCase) {
        try {
            return new ObjectMapper().writeValueAsString(existingCase);
        } catch (JsonProcessingException e) {
            return "{\"id\":\"" + existingCase.getId() + "\"}";
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
