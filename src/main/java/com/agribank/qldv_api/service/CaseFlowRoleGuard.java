package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.enums.ERoleCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RR-03 — quy tắc "ai là chuyên viên xử lý chính của hồ sơ theo luồng" (R-CV ↔ Luồng A, R-BPTM ↔
 * Luồng B/C), KHÁC với role phê duyệt/kiểm soát theo từng trạng thái (đã có
 * {@code CaseWorkflowConfig.RULES} lo riêng). Tách hàm dùng chung để các API gắn với vai trò
 * chuyên viên (VD API-SC06-01 kích hoạt lưu trữ) không tự lặp lại rule này, đúng
 * coding-convention.md mục 11.
 */
@Component
public class CaseFlowRoleGuard {
    private static final Set<String> FLOW_A_ROLES = Set.of(ERoleCode.R_CV.getCode());
    private static final Set<String> FLOW_BC_ROLES = Set.of(ERoleCode.R_BPTM.getCode());

    public void requireCaseworkerRoleForFlow(Case existingCase, List<String> roleCodes) {
        Set<String> roles = roleCodes == null ? Set.of() : new HashSet<>(roleCodes);
        boolean isFlowA = Constants.CASE_FLOW_BTCDU.equals(existingCase.getOriginFlow());
        Set<String> allowedRoles = isFlowA ? FLOW_A_ROLES : FLOW_BC_ROLES;
        boolean allowed = roles.stream().anyMatch(allowedRoles::contains);
        if (!allowed) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền thao tác hồ sơ thuộc luồng này");
        }
    }
}
