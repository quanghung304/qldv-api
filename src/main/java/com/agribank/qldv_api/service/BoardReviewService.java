package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.*;
import com.agribank.qldv_api.gateway.CaseBoardReviewClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.BoardReviewRequest;
import com.agribank.qldv_api.response.casemgmt.BoardReviewResponse;
import com.agribank.qldv_api.workflow.WorkflowAssigneeGuard;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseBoardReview;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.enums.EBoardReviewMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BoardReviewService {
    private final CaseService caseService;
    private final CaseClient caseClient;
    private final CaseEstablishmentClient establishmentClient;
    private final CaseBoardReviewClient boardReviewClient;
    private final UserService userService;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;

    /**
     * BR-SC04 mở rộng: cho sửa khi hồ sơ đang ở A-04 ("Trình Ban Thường vụ", R-CV nhập liệu) HOẶC
     * A-06 ("Trình kiểm soát (trước họp BCH)") để Kiểm soát viên (R-KS) đang được giao xử lý hồ sơ
     * ở bước kiểm soát cũng sửa được ý kiến Ban Thường vụ.
     */
    public BoardReviewResponse upsert(String caseId, BoardReviewRequest request) {
        Case existingCase = requireCaseInScope(caseId);
        boolean atWorkingStatus = ECaseStatusCode.A_04.getCode().equals(existingCase.getStatusId());
        boolean atControlStatus = ECaseStatusCode.A_06.getCode().equals(existingCase.getStatusId());
        if (!atWorkingStatus && !atControlStatus) {
            throw new ForbiddenException("ERR-SC03-01: Hồ sơ không ở trạng thái A-04 hoặc A-06");
        }
        workflowAssigneeGuard.requireAssignee(existingCase, requireUser().getId());
        CaseEstablishment establishment = establishmentClient.findByCaseId(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy dữ liệu Bước 1 của hồ sơ"));

        if (EBoardReviewMethod.MEETING.matches(request.getMethod()) && request.getBoardDocumentDate() != null
                && establishment.getBoardDecisionDate() != null
                && request.getBoardDocumentDate().isBefore(establishment.getBoardDecisionDate())) {
            throw new FieldValidationException(Map.of("board_document_date",
                    "ERR-SC04-03: board_document_date không được trước board_decision_date của Bước 1"));
        }

        CaseBoardReview saved = boardReviewClient.upsert(toEntity(caseId, request)).getData();
        return new BoardReviewResponse(caseId, existingCase.getStatusId(), saved);
    }

    public BoardReviewResponse get(String caseId) {
        Case existingCase = requireCaseInScope(caseId);
        CaseBoardReview review = boardReviewClient.findByCaseId(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy dữ liệu ghi nhận ý kiến Ban Thường vụ"));
        return new BoardReviewResponse(caseId, existingCase.getStatusId(), review);
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private Case requireCaseInScope(String caseId) {
        caseService.getById(caseId);

        return caseClient.findById(caseId).getData().orElseThrow(
                () -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    private CaseBoardReview toEntity(String caseId, BoardReviewRequest request) {
        CaseBoardReview value = CaseBoardReview.builder()
                .caseId(caseId)
                .method(request.getMethod())
                .build();

        if (EBoardReviewMethod.MEETING.matches(request.getMethod())) {
            value.setBoardDocumentNo(request.getBoardDocumentNo());
            value.setBoardDocumentDate(request.getBoardDocumentDate());
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
