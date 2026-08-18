package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.AttachmentClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.gateway.DocumentClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.DecisionDocumentsRequest;
import com.agribank.qldv_api.response.casemgmt.DecisionDocumentsResponse;
import com.agribank.qldv_api.workflow.WorkflowEngine;
import com.agribank.qldvutils.entity.Attachment;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.entity.Document;
import com.agribank.qldvutils.enums.ECaseWorkflowAction;
import com.agribank.qldvutils.enums.EDocumentOrigin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * API-SC05-02 — Bước 3 giai đoạn 2 (sau ban hành). 3 cặp field của request map vào 3 bản ghi
 * PMDV_DOCUMENT riêng biệt (establish decision, committee appointment decision, political
 * standard conclusion) — KHÔNG có bảng phẳng riêng (xem prompt_S3-02 mục 4).
 *
 * PMDV_DOCUMENT_TYPE đã bị loại bỏ khỏi dự án (task tái cấu trúc PMDV_DOCUMENT_TEMPLATE) — 3
 * document ở đây là REFERENCE/GENERATED nhập tay, KHÔNG qua template, nên dùng {@code documentName}
 * (chuỗi cố định, không phụ thuộc danh mục nào) làm khóa phân biệt thay vì document_type_id cũ.
 *
 * GC-S3-02-06: political_standard_conclusion đáng lẽ đã có sẵn record (origin=REFERENCE) từ lúc
 * tạo hồ sơ ở SC-02 — nhưng cơ chế Data Mapping/tự tạo record căn cứ đó (S2-03) chưa nối vào
 * EstablishmentCaseService (xem TODO ở đó). Ở đây dùng chiến lược upsert (tìm theo case_id +
 * document_name, có thì update, không có thì tạo mới) để endpoint chạy được ngay cả khi
 * record căn cứ đó chưa tồn tại, đồng thời vẫn đúng hành vi "update" khi record đã có sẵn.
 *
 * GC-S3-02-04 (dung hòa): luôn cho lưu dữ liệu hợp lệ của bất kỳ bộ nào được gửi (kể cả chưa đủ
 * cả 3 bộ) — chỉ tự động gọi thẳng {@link WorkflowEngine} (action REGISTER_SIGNED_DOC, ĐẶC BIỆT
 * không qua endpoint workflow-action dùng chung, theo đúng thiết kế của workflow-states.md) khi
 * SAU KHI lưu, cả 3 document đều đủ document_no/document_date/effective_date VÀ mỗi document đều
 * có >= 1 attachment (scan) liên kết.
 */
@Service
@RequiredArgsConstructor
public class DecisionDocumentsService {
    private static final String ESTABLISH_DECISION_NAME = Constants.ESTABLISH_DECISION_DOCUMENT_NAME;
    private static final String COMMITTEE_APPOINTMENT_DECISION_NAME = "Quyết định chuẩn y cấp ủy";
    private static final String POLITICAL_STANDARD_CONCLUSION_NAME = "Kết luận tiêu chuẩn chính trị";

    private final CaseService caseService;
    private final CaseClient caseClient;
    private final CaseEstablishmentClient caseEstablishmentClient;
    private final DocumentClient documentClient;
    private final AttachmentClient attachmentClient;
    private final WorkflowEngine workflowEngine;
    private final UserService userService;

    public DecisionDocumentsResponse upsert(String caseId, DecisionDocumentsRequest request) {
        Case existingCase = requireCaseInScope(caseId);
        if (!ECaseStatusCode.A_15.getCode().equals(existingCase.getStatusId())) {
            throw new ForbiddenException("ERR-SC03-01: Hồ sơ không ở trạng thái A-15");
        }
        UserDetailsImpl user = requireUser();

        Document existingEstablishDoc = findDocument(caseId, ESTABLISH_DECISION_NAME);
        Document existingCommitteeDoc = findDocument(caseId, COMMITTEE_APPOINTMENT_DECISION_NAME);
        Document existingPoliticalDoc = findDocument(caseId, POLITICAL_STANDARD_CONCLUSION_NAME);

        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        validateUniqueDocumentNo(errors, "establishDecisionNo", request.getEstablishDecisionNo(), existingEstablishDoc);
        validateUniqueDocumentNo(errors, "committeeAppointmentDecisionNo", request.getCommitteeAppointmentDecisionNo(), existingCommitteeDoc);
        validateUniqueDocumentNo(errors, "politicalStandardConclusionNoFinal", request.getPoliticalStandardConclusionNoFinal(), existingPoliticalDoc);

        CaseEstablishment establishment = caseEstablishmentClient.findByCaseId(caseId).getData().orElse(null);
        validatePoliticalStandardValidity(warnings, request.getPoliticalStandardConclusionEffectiveDate(),
                establishment != null ? establishment.getPoliticalStandardConclusionDate() : null);

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        Document establishDoc = upsertDocument(existingEstablishDoc, caseId, ESTABLISH_DECISION_NAME,
                request.getEstablishDecisionNo(), request.getEstablishDecisionIssueDate(),
                request.getEstablishDecisionEffectiveDate(), user.getId(), EDocumentOrigin.GENERATED.getId());
        Document committeeDoc = upsertDocument(existingCommitteeDoc, caseId, COMMITTEE_APPOINTMENT_DECISION_NAME,
                request.getCommitteeAppointmentDecisionNo(), request.getCommitteeAppointmentIssueDate(),
                request.getCommitteeAppointmentEffectiveDate(), user.getId(), EDocumentOrigin.GENERATED.getId());
        Document politicalDoc = upsertDocument(existingPoliticalDoc, caseId, POLITICAL_STANDARD_CONCLUSION_NAME,
                request.getPoliticalStandardConclusionNoFinal(), request.getPoliticalStandardConclusionIssueDate(),
                request.getPoliticalStandardConclusionEffectiveDate(), user.getId(), EDocumentOrigin.REFERENCE.getId());

        Map<String, String> missing = computeMissing(establishDoc, committeeDoc, politicalDoc);

        String statusId = existingCase.getStatusId();
        if (missing.isEmpty()) {
            workflowEngine.transition(caseId, ECaseWorkflowAction.REGISTER_SIGNED_DOC.name(),
                    user.getRoleCodes(), user.getId(), null);
            statusId = ECaseStatusCode.A_16.getCode();
        }

        return buildResponse(caseId, statusId, establishDoc, committeeDoc, politicalDoc, missing, warnings);
    }

    /** Xem lại dữ liệu 3 văn bản Bước 3 GĐ2 đã nhập (nếu có) — không đổi trạng thái, không guard status A-15. */
    public DecisionDocumentsResponse get(String caseId) {
        Case existingCase = requireCaseInScope(caseId);

        Document establishDoc = findDocument(caseId, ESTABLISH_DECISION_NAME);
        Document committeeDoc = findDocument(caseId, COMMITTEE_APPOINTMENT_DECISION_NAME);
        Document politicalDoc = findDocument(caseId, POLITICAL_STANDARD_CONCLUSION_NAME);

        Map<String, String> missing = computeMissing(establishDoc, committeeDoc, politicalDoc);

        return buildResponse(caseId, existingCase.getStatusId(), establishDoc, committeeDoc, politicalDoc, missing, Map.of());
    }

    /** Ánh xạ lại 3 Document (schema chung document_name/document_no/document_date) sang ĐÚNG tên field của request, dễ đối chiếu. */
    private DecisionDocumentsResponse buildResponse(String caseId, String statusId, Document establishDoc,
                                                      Document committeeDoc, Document politicalDoc,
                                                      Map<String, String> missing, Map<String, String> warnings) {
        DecisionDocumentsResponse response = new DecisionDocumentsResponse();
        response.setCaseId(caseId);
        response.setStatusId(statusId);
        if (establishDoc != null) {
            response.setEstablishDecisionId(establishDoc.getId());
            response.setEstablishDecisionNo(establishDoc.getDocumentNo());
            response.setEstablishDecisionIssueDate(establishDoc.getDocumentDate());
            response.setEstablishDecisionEffectiveDate(establishDoc.getEffectiveDate());
        }
        if (committeeDoc != null) {
            response.setCommitteeAppointmentDecisionId(committeeDoc.getId());
            response.setCommitteeAppointmentDecisionNo(committeeDoc.getDocumentNo());
            response.setCommitteeAppointmentIssueDate(committeeDoc.getDocumentDate());
            response.setCommitteeAppointmentEffectiveDate(committeeDoc.getEffectiveDate());
        }
        if (politicalDoc != null) {
            response.setPoliticalStandardConclusionId(politicalDoc.getId());
            response.setPoliticalStandardConclusionNoFinal(politicalDoc.getDocumentNo());
            response.setPoliticalStandardConclusionIssueDate(politicalDoc.getDocumentDate());
            response.setPoliticalStandardConclusionEffectiveDate(politicalDoc.getEffectiveDate());
        }
        response.setMissing(missing);
        response.setWarnings(warnings);
        return response;
    }

    // ---------------------------------------------------------------- helpers

    private Case requireCaseInScope(String caseId) {
        caseService.getById(caseId);
        return caseClient.findById(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private Document findDocument(String caseId, String documentName) {
        return documentClient.findByCaseIdAndDocumentName(caseId, documentName).getData().orElse(null);
    }

    private void validateUniqueDocumentNo(Map<String, String> errors, String field, String no, Document existing) {
        if (no == null) {
            return;
        }
        String excludeId = existing != null ? existing.getId() : null;
        Boolean exists = documentClient.existsByDocumentNo(no, excludeId).getData();
        if (Boolean.TRUE.equals(exists)) {
            errors.put(field, "ERR-SC05-01: " + field + " đã tồn tại ở hồ sơ khác trong hệ thống");
        }
    }

    /** BR-SC02-02 — cảnh báo (không chặn lưu), so với ngày kết luận TCCT gốc (field 14, SC-02). */
    private void validatePoliticalStandardValidity(Map<String, String> warnings, LocalDate effectiveDate,
                                                    LocalDate originalConclusionDate) {
        if (effectiveDate == null || originalConclusionDate == null) {
            return;
        }
        if (originalConclusionDate.plusMonths(Constants.POLITICAL_STANDARD_VALIDITY_MONTHS).isBefore(effectiveDate)) {
            warnings.put("ERR-SC02-13", "political_standard_conclusion_effective_date đã vượt quá 6 tháng kể từ "
                    + "ngày kết luận TCCT gốc (BR-SC02-02)");
        }
    }

    private Document upsertDocument(Document existing, String caseId, String documentName, String no,
                                     LocalDate issueDate, LocalDate effectiveDate, String createdBy, Integer origin) {
        if (no == null && issueDate == null && effectiveDate == null) {
            return existing;
        }
        Document target = existing != null ? existing : Document.builder()
                .caseId(caseId)
                .documentName(documentName)
                .origin(origin)
                .createdBy(createdBy)
                .build();
        target.setDocumentNo(no);
        target.setDocumentDate(issueDate);
        target.setEffectiveDate(effectiveDate);
        return documentClient.save(target).getData();
    }

    /** BR-SC05-02 — đủ điều kiện tự động chuyển A-15 → A-16 khi CẢ 3 document đã đủ dữ liệu + có scan. */
    private Map<String, String> computeMissing(Document establishDoc, Document committeeDoc, Document politicalDoc) {
        List<String> allDocIds = Stream.of(establishDoc, committeeDoc, politicalDoc)
                .filter(Objects::nonNull).map(Document::getId).toList();
        Map<String, Long> attachmentCountByDocId = allDocIds.isEmpty() ? Map.of()
                : safeList(attachmentClient.findByDocumentIds(allDocIds).getData()).stream()
                        .collect(Collectors.groupingBy(Attachment::getDocumentId, Collectors.counting()));

        Map<String, String> missing = new LinkedHashMap<>();
        checkMissing(missing, "establishDecision", establishDoc, attachmentCountByDocId);
        checkMissing(missing, "committeeAppointmentDecision", committeeDoc, attachmentCountByDocId);
        checkMissing(missing, "politicalStandardConclusion", politicalDoc, attachmentCountByDocId);
        return missing;
    }

    private void checkMissing(Map<String, String> missing, String label, Document document,
                               Map<String, Long> attachmentCountByDocId) {
        if (document == null || document.getDocumentNo() == null || document.getDocumentDate() == null
                || document.getEffectiveDate() == null) {
            missing.put(label, "ERR-SC05-03: Chưa đủ document_no/document_date/effective_date");
            return;
        }
        long attachmentCount = attachmentCountByDocId.getOrDefault(document.getId(), 0L);
        if (attachmentCount < 1) {
            missing.put(label, "ERR-SC05-03: Chưa có tệp scan văn bản đã ký đính kèm");
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
