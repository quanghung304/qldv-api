package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.CaseBoardReviewClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseCommitteeReviewClient;
import com.agribank.qldv_api.request.casemgmt.CommitteeReviewRequest;
import com.agribank.qldv_api.response.casemgmt.CommitteeReviewResponse;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseBoardReview;
import com.agribank.qldvutils.entity.CaseCommitteeReview;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * API-SC05-01 — Bước 3 giai đoạn 1 (trước ban hành). Mirror nguyên cấu trúc
 * {@link BoardReviewService} (S3-01) theo GC-S3-02-01: chỉ upsert dữ liệu ghi nhận ý kiến BCH,
 * KHÔNG tự chuyển trạng thái ở đây — việc chuyển A-08 → A-10 do endpoint dùng chung
 * (POST /cases/{id}/workflow-action, action=SUBMIT_CONTROL) đảm nhiệm sau khi Chuyên viên đã
 * nhập xong dữ liệu bước này, đúng như cách S3-01 đã tách data-entry khỏi transition.
 */
@Service
@RequiredArgsConstructor
public class CommitteeReviewService {
    private final CaseService caseService;
    private final CaseClient caseClient;
    private final CaseBoardReviewClient boardReviewClient;
    private final CaseCommitteeReviewClient committeeReviewClient;

    public CommitteeReviewResponse upsert(String caseId, CommitteeReviewRequest request) {
        Case existingCase = requireCaseInScope(caseId);
        if (!ECaseStatusCode.A_08.getCode().equals(existingCase.getStatusId())) {
            throw new ForbiddenException("ERR-SC03-01: Hồ sơ không ở trạng thái A-08");
        }
        validateCommitteeDocumentDateAgainstBoardReview(caseId, request);

        CaseCommitteeReview saved = committeeReviewClient.upsert(toEntity(caseId, request)).getData();
        return new CommitteeReviewResponse(caseId, existingCase.getStatusId(), saved);
    }

    /**
     * GC-S3-02-05: mốc so sánh ngày dùng ngày văn bản Bước 2 (CaseBoardReview), không phải Bước 1
     * (CaseEstablishment.boardDecisionDate) — Bước 3 GĐ1 nối tiếp Bước 2. Guard clause từng điều
     * kiện thay vì lồng if để tránh if-trong-if.
     */
    private void validateCommitteeDocumentDateAgainstBoardReview(String caseId, CommitteeReviewRequest request) {
        if (!EBoardReviewMethod.MEETING.matches(request.getCommitteeMethod())) {
            return;
        }
        if (request.getCommitteeDocumentDate() == null) {
            return;
        }
        CaseBoardReview boardReview = boardReviewClient.findByCaseId(caseId).getData().orElse(null);
        if (boardReview == null || boardReview.getBoardDocumentDate() == null) {
            return;
        }
        if (request.getCommitteeDocumentDate().isBefore(boardReview.getBoardDocumentDate())) {
            throw new FieldValidationException(Map.of("committeeDocumentDate",
                    "ERR-SC04-03: committee_document_date không được trước ngày văn bản Bước 2 (board_document_date)"));
        }
    }

    public CommitteeReviewResponse get(String caseId) {
        Case existingCase = requireCaseInScope(caseId);
        CaseCommitteeReview review = committeeReviewClient.findByCaseId(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy dữ liệu ghi nhận ý kiến Ban Chấp hành"));
        return new CommitteeReviewResponse(caseId, existingCase.getStatusId(), review);
    }

    private Case requireCaseInScope(String caseId) {
        caseService.getById(caseId);

        return caseClient.findById(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private CaseCommitteeReview toEntity(String caseId, CommitteeReviewRequest request) {
        CaseCommitteeReview value = CaseCommitteeReview.builder()
                .caseId(caseId)
                .method(request.getCommitteeMethod())
                .build();

        if (EBoardReviewMethod.MEETING.matches(request.getCommitteeMethod())) {
            value.setCommitteeDocumentNo(request.getCommitteeDocumentNo());
            value.setCommitteeDocumentDate(request.getCommitteeDocumentDate());
        } else {
            value.setBallotsIssued(request.getBallotsIssued());
            value.setBallotsReturned(request.getBallotsReturned());
            value.setBallotsAgree(request.getBallotsAgree());
            value.setBallotsDisagree(request.getBallotsDisagree());
            value.setOpinionNotes(request.getOpinionNotes());
        }
        return value;
    }
}
