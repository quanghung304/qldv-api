package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentCommitteeClient;
import com.agribank.qldv_api.gateway.DocumentClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.response.casemgmt.CompleteCaseResponse;
import com.agribank.qldv_api.workflow.CaseWorkflowConfig;
import com.agribank.qldv_api.workflow.WorkflowTransitionRule;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CommitteeMember;
import com.agribank.qldvutils.entity.Document;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.enums.ECommitteeMemberStatus;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.casemgmt.CaseCompletePersistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API-SC06-02 (POST /cases/{id}/complete, prompt_S3-03) — bước quan trọng nhất của luồng Thành
 * lập TCĐ: tổ chức đảng CHÍNH THỨC ra đời ở đây (PMDV_ORGANIZATION), không sớm hơn. Toàn bộ side
 * effect (mục 5, prompt) gói vào 1 {@link CaseCompletePersistRequest} + 1 lệnh Feign duy nhất,
 * chạy trong ĐÚNG 1 transaction ở qldv-db ({@code CaseCompleteService}) — service đó TỰ cập nhật
 * status_id/PMDV_CASE_HISTORY luôn (KHÔNG gọi WorkflowEngine.transition() riêng — đó là 1 Feign
 * call TÁCH BIỆT sẽ phá vỡ tính atomic bắt buộc của mục 7 nếu tạo Organization xong mà transition
 * lỗi, hoặc ngược lại). Vẫn dùng ĐÚNG {@link CaseWorkflowConfig#RULES} làm nguồn chân lý duy nhất
 * để tìm role/trạng thái đích — không hardcode lại rule này.
 *
 * [GC-S3-03-03] organization_name lấy từ {@code Case.proposedOrganizationName} — cột này ĐÃ tồn
 * tại và ĐÃ được {@code EstablishmentCaseService} ghi từ lúc tạo case (field 7, SC-02). Gap nêu ở
 * prompt_S3-03 mục 6 KHÔNG áp dụng cho codebase hiện tại — không cần thêm cột mới.
 *
 * [GC-S3-03-04] Không cần đặt mã action riêng cho A-15→A-16 — transition đó đã tồn tại sẵn
 * (REGISTER_SIGNED_DOC, DecisionDocumentsService) từ trước khi task này được code, xem
 * {@link ArchiveCaseService}.
 *
 * Khóa hồ sơ (BR-SC06-03) KHÔNG cần thêm cột/flag mới: mọi endpoint ghi dữ liệu Bước 1-3
 * (EstablishmentCaseService/BoardReviewService/CommitteeReviewService/DecisionDocumentsService)
 * đã tự guard đúng 1 status_id bắt buộc riêng (A-01/A-04/A-08/A-15) — A-17 không khớp bất kỳ guard
 * nào trong số đó nên tự động read-only "by construction", không cần cơ chế khóa riêng.
 * BR-SC06-04 (không cho xóa PMDV_ATTACHMENT dù Admin) hiện chưa có endpoint xóa attachment nào
 * trong dự án để áp dụng guard này — ghi nhận là gap, ngoài phạm vi 2 API của task này.
 */
@Service
@RequiredArgsConstructor
public class CaseCompleteService {
    private static final String APPROVE_COMPLETE_ACTION = "APPROVE_COMPLETE";
    private static final String ORGANIZATION_CODE_PREFIX = "TCD";

    private final CaseClient caseClient;
    private final CaseEstablishmentClient caseEstablishmentClient;
    private final CaseEstablishmentCommitteeClient caseEstablishmentCommitteeClient;
    private final DocumentClient documentClient;
    private final OrganizationClient organizationClient;
    private final UserService userService;

    public CompleteCaseResponse complete(String caseId) {
        Case existingCase = requireCase(caseId);
        UserDetailsImpl user = requireUser();

        WorkflowTransitionRule rule = requireMatchingRule(existingCase);
        String matchedRole = requireMatchedRole(rule, user.getRoleCodes());

        CaseEstablishment establishment = requireEstablishment(caseId);
        Document establishDoc = requireEstablishDecisionDocument(caseId);
        String organizationName = requireOrganizationName(existingCase);
        requireOrganizationNameUnique(organizationName);

        Organization organization = buildOrganization(establishment, establishDoc, organizationName);
        List<CommitteeMember> committeeMembers = buildCommitteeMembers(caseId);

        CaseCompletePersistRequest request = new CaseCompletePersistRequest();
        request.setCaseId(caseId);
        request.setOrganization(organization);
        request.setCommitteeMembers(committeeMembers);
        request.setFromStatusId(existingCase.getStatusId());
        request.setToStatusId(rule.toStatusCode().getCode());
        request.setAction(APPROVE_COMPLETE_ACTION);
        request.setPerformedBy(user.getId());
        request.setPerformedRoleId(matchedRole);

        Organization saved = caseClient.completeCase(request).getData();
        return new CompleteCaseResponse(caseId, rule.toStatusCode().getCode(), saved.getId(), saved.getOrganizationCode());
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

    /** Dùng ĐÚNG CaseWorkflowConfig.RULES làm nguồn chân lý — không hardcode lại role/trạng thái đích ở đây. */
    private WorkflowTransitionRule requireMatchingRule(Case existingCase) {
        return CaseWorkflowConfig.RULES.stream()
                .filter(r -> r.flowCode().equals(existingCase.getOriginFlow())
                        && r.fromStatusCode().getCode().equals(existingCase.getStatusId())
                        && r.actionCode().equals(APPROVE_COMPLETE_ACTION))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại"));
    }

    private String requireMatchedRole(WorkflowTransitionRule rule, List<String> roleCodes) {
        return (roleCodes == null ? List.<String>of() : roleCodes).stream()
                .filter(rule.requiredRoleCodes()::contains)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException(
                        "ERR-SC03-01: Bạn không có quyền thực hiện thao tác này ở bước hiện tại"));
    }

    private CaseEstablishment requireEstablishment(String caseId) {
        return caseEstablishmentClient.findByCaseId(caseId).getData()
                .orElseThrow(() -> new CommonException("Chưa có dữ liệu Bước 1 (PMDV_CASE_ESTABLISHMENT) của hồ sơ này"));
    }

    private Document requireEstablishDecisionDocument(String caseId) {
        Document document = documentClient.findByCaseIdAndDocumentName(caseId, Constants.ESTABLISH_DECISION_DOCUMENT_NAME)
                .getData().orElse(null);
        if (document == null || document.getDocumentNo() == null || document.getDocumentDate() == null) {
            throw new CommonException("Chưa có đủ dữ liệu Quyết định thành lập tổ chức đảng (Bước 3) của hồ sơ này");
        }
        return document;
    }

    private String requireOrganizationName(Case existingCase) {
        String name = existingCase.getProposedOrganizationName();
        if (name == null || name.isBlank()) {
            throw new CommonException("Hồ sơ chưa có tên tổ chức đảng dự kiến (field 7, SC-02)");
        }
        return name;
    }

    /** BR-SC02-05, tái kiểm tra ngay trước khi tạo chính thức — không giả định lần kiểm tra lúc tạo case vẫn còn đúng. */
    private void requireOrganizationNameUnique(String organizationName) {
        Boolean exists = organizationClient.existsActiveByName(organizationName, EOperationStatus.ACTIVE.getId()).getData();
        if (Boolean.TRUE.equals(exists)) {
            throw new FieldValidationException(Map.of("organizationName",
                    "ERR-SC02-05: Tên tổ chức đảng đã trùng với 1 tổ chức đang Hoạt động"));
        }
    }

    private Organization buildOrganization(CaseEstablishment establishment, Document establishDoc, String organizationName) {
        return Organization.builder()
                .organizationCode(generateOrganizationCode())
                .organizationName(organizationName)
                .organizationTypeId(establishment.getOrganizationTypeId())
                .brcd(establishment.getBrcd())
                .memberCount(establishment.getMemberCount())
                .committeeMemberCount(establishment.getCommitteeMemberCount())
                .operationStatus(EOperationStatus.ACTIVE.getId())
                .establishDecisionNo(establishDoc.getDocumentNo())
                .establishDecisionDate(establishDoc.getDocumentDate())
                .build();
    }

    private List<CommitteeMember> buildCommitteeMembers(String caseId) {
        List<CaseEstablishmentCommittee> rows = safeList(caseEstablishmentCommitteeClient.findByCaseId(caseId).getData());
        return rows.stream()
                .map(row -> CommitteeMember.builder()
                        .staffCode(row.getStaffCode())
                        .position(row.getProposedPosition())
                        .status(ECommitteeMemberStatus.OFFICIAL.getId())
                        .build())
                .toList();
    }

    /**
     * Chưa có quy tắc sinh mã chính thức cho PMDV_ORGANIZATION ở bất kỳ tài liệu nguồn nào — tạm
     * dùng cùng kiểu sequential-per-prefix như {@code Case.generateCaseCode()}, CẦN xác nhận lại
     * với team trước khi go-live (không có căn cứ tài liệu, chỉ suy luận theo mẫu hiện có).
     */
    private String generateOrganizationCode() {
        String prefix = ORGANIZATION_CODE_PREFIX + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Optional<Organization> latest = organizationClient.findLatestByPrefix(prefix + "-").getData();
        int nextSeq = 1;
        if (latest.isPresent()) {
            String latestCode = latest.get().getOrganizationCode();
            nextSeq = Integer.parseInt(latestCode.substring(latestCode.lastIndexOf('-') + 1)) + 1;
        }
        return prefix + "-" + String.format("%04d", nextSeq);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
