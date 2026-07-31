package com.agribank.qldv_api.service.doctemplate;

import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.service.BranchService;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.enums.ELinkRole;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * EXTERNAL_LOOKUP resolver "BRANCH_NAME_BY_BRCD" (S2-03) — đặt ở qldv-api (KHÔNG phải qldv-db) để
 * tái sử dụng {@link BranchService}/{@code IAMClient} đã có sẵn, thay vì tạo RestTemplate/client
 * riêng gọi thẳng ra ngoài từ qldv-db. "Đơn vị chuyên môn" trong domain này CHÍNH LÀ chi nhánh
 * (brcd) — không phải 1 khái niệm tách biệt, nên đặt tên theo "branch" xuyên suốt.
 *
 * Lấy brcd từ organization liên quan tới case (ưu tiên TARGET, fallback SOURCE), gọi IAM qua
 * {@link BranchService#getBranchName(Integer)}. BẤT KỲ lỗi nào (không tìm được brcd, IAM lỗi/timeout)
 * đều KHÔNG throw ra ngoài — trả chuỗi cố định + log WARN, không chặn request sinh văn bản.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchNameByBrcdResolver {
    private static final String FALLBACK_TEXT = "[Cần bổ sung tên chi nhánh]";

    private final CaseOrganizationClient caseOrganizationClient;
    private final OrganizationClient organizationClient;
    private final BranchService branchService;

    public String resolve(String caseId) {
        Integer brcd = resolveBrcd(caseId, ELinkRole.TARGET.getId());
        if (brcd == null) {
            brcd = resolveBrcd(caseId, ELinkRole.SOURCE.getId());
        }
        if (brcd == null) {
            log.warn("BRANCH_NAME_BY_BRCD: case {} không có tổ chức TARGET/SOURCE nào để lấy brcd", caseId);
            return FALLBACK_TEXT;
        }
        try {
            String branchName = branchService.getBranchName(brcd);
            if (branchName == null || branchName.isBlank()) {
                log.warn("BRANCH_NAME_BY_BRCD: IAM không trả tên chi nhánh cho brcd={}", brcd);
                return FALLBACK_TEXT;
            }
            return branchName;
        } catch (Exception e) {
            log.warn("BRANCH_NAME_BY_BRCD: lỗi gọi IAM cho brcd={} — {}", brcd, e.getMessage());
            return FALLBACK_TEXT;
        }
    }

    private Integer resolveBrcd(String caseId, int linkRole) {
        List<CaseOrganizationSummaryResponse> links = safeList(caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());
        return links.stream()
                .filter(link -> link.getLinkRole() != null && link.getLinkRole() == linkRole)
                .findFirst()
                .map(CaseOrganizationSummaryResponse::getOrganizationId)
                .flatMap(organizationId -> organizationClient.findById(organizationId).getData())
                .map(Organization::getBrcd)
                .orElse(null);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
