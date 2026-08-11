package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.request.casemgmt.CaseChangeRequest;
import com.agribank.qldv_api.request.casemgmt.CaseSearchRequest;
import com.agribank.qldv_api.request.casemgmt.BoardReviewRequest;
import com.agribank.qldv_api.request.casemgmt.CommitteeReviewRequest;
import com.agribank.qldv_api.request.casemgmt.DecisionDocumentsRequest;
import com.agribank.qldv_api.request.casemgmt.EstablishmentCaseRequest;
import com.agribank.qldv_api.request.casemgmt.WorkflowActionRequest;
import com.agribank.qldv_api.response.casemgmt.ArchiveCaseResponse;
import com.agribank.qldv_api.response.casemgmt.CaseChangeResponse;
import com.agribank.qldv_api.response.casemgmt.CaseDeleteResponse;
import com.agribank.qldv_api.response.casemgmt.CaseDetailResponse;
import com.agribank.qldv_api.response.casemgmt.BoardReviewResponse;
import com.agribank.qldv_api.response.casemgmt.CommitteeReviewResponse;
import com.agribank.qldv_api.response.casemgmt.CompleteCaseResponse;
import com.agribank.qldv_api.response.casemgmt.DecisionDocumentsResponse;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseDetailResponse;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseResponse;
import com.agribank.qldv_api.security.RequirePermission;
import com.agribank.qldv_api.service.CaseChangeService;
import com.agribank.qldv_api.service.CaseDeleteService;
import com.agribank.qldv_api.service.ArchiveCaseService;
import com.agribank.qldv_api.service.CaseCompleteService;
import com.agribank.qldv_api.service.CaseService;
import com.agribank.qldv_api.service.BoardReviewService;
import com.agribank.qldv_api.service.CommitteeReviewService;
import com.agribank.qldv_api.service.DecisionDocumentsService;
import com.agribank.qldv_api.service.EstablishmentCaseService;
import com.agribank.qldvutils.enums.EAuthorityLevel;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.casemgmt.CaseHistoryItemResponse;
import com.agribank.qldvutils.response.casemgmt.CaseListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cases")
public class CaseController {
    private final CaseService caseService;
    private final CaseDeleteService caseDeleteService;
    private final EstablishmentCaseService establishmentCaseService;
    private final CaseChangeService caseChangeService;
    private final BoardReviewService boardReviewService;
    private final CommitteeReviewService committeeReviewService;
    private final DecisionDocumentsService decisionDocumentsService;
    private final ArchiveCaseService archiveCaseService;
    private final CaseCompleteService caseCompleteService;

    @RequirePermission(function = "FN4", action = "VIEW")
    @PostMapping
    public ResponseEntity<BaseResponse<PageResponse<CaseListItemResponse>>> search(
            @RequestBody CaseSearchRequest request) {
        request.validate();
        return BaseResponse.success(caseService.search(request));
    }

    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CaseDetailResponse>> getById(@PathVariable String id) {
        return BaseResponse.success(caseService.getById(id));
    }

    @RequirePermission(function = "FN4", action = "VIEW")
    @GetMapping("/{id}/history")
    public ResponseEntity<BaseResponse<List<CaseHistoryItemResponse>>> getHistory(@PathVariable String id) {
        return BaseResponse.success(caseService.getHistory(id));
    }

    /**
     * API-GL-01 — xóa hồ sơ CHỈ khi còn "Đang thực hiện" lần đầu tiên (chưa từng Trình kiểm
     * soát), áp dụng chung cho mọi loại nghiệp vụ (Thành lập TCĐ lẫn biến động). Guard chi tiết
     * (role khớp luồng, status_id, PMDV_CASE_HISTORY rỗng) nằm ở CaseDeleteService.
     */
    @RequirePermission(function = "FN1", action = "DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<CaseDeleteResponse>> deleteCase(@PathVariable String id) {
        return BaseResponse.success(caseDeleteService.delete(id));
    }

    @RequirePermission(function = "FN2", action = "APPROVE")
    @PostMapping("/{id}/workflow-action")
    public ResponseEntity<BaseResponse<String>> workflowAction(@PathVariable String id,
                                                                 @RequestBody WorkflowActionRequest request) {
        request.validate();
        caseService.performWorkflowAction(id, request);
        return BaseResponse.success("Success");
    }

    /** Xem chi tiết đầy đủ 15 field Bước 1 SC-02 — dùng chung cho cả cấp Agribank lẫn SC-07 sau này. */
    @RequirePermission(function = "FN1", action = "VIEW")
    @GetMapping("/{id}/establishment")
    public ResponseEntity<BaseResponse<EstablishmentCaseDetailResponse>> getEstablishmentDetail(@PathVariable String id) {
        return BaseResponse.success(establishmentCaseService.getEstablishmentCaseDetail(id));
    }

    /**
     * API-SC02-01 — luôn gọi service dùng chung với authorityLevel=BANK_LEVEL,
     * originFlow=CASE_FLOW_BTCDU ("A") và allowedOrganizationTypeId=null (client tự chọn trong
     * danh mục). SC-07 (Sprint 5, cấp cơ sở) sẽ có controller riêng gọi lại CÙNG service này với
     * authorityLevel/originFlow/allowedOrganizationTypeId khác — không sửa lại core logic (RR-03).
     */
    @RequirePermission(function = "FN1", action = "CREATE")
    @PostMapping("/establishments")
    public ResponseEntity<BaseResponse<EstablishmentCaseResponse>> createEstablishment(@RequestBody EstablishmentCaseRequest request) {
        request.validate();
        return BaseResponse.success(establishmentCaseService.createEstablishmentCase(
                request, EAuthorityLevel.BANK_LEVEL.getId(), Constants.CASE_FLOW_BTCDU, null));
    }

    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/establishment")
    public ResponseEntity<BaseResponse<EstablishmentCaseResponse>> updateEstablishment(@PathVariable String id, @RequestBody EstablishmentCaseRequest request) {
        request.validateForUpdate();
        return BaseResponse.success(establishmentCaseService.updateEstablishmentCase(id, request, null));
    }

    /**
     * API-SC08-01 — Bước 1 dùng CHUNG cho 5 nghiệp vụ biến động TCĐ (Giải thể/Sáp nhập/Hợp
     * nhất/Chia tách/Đổi tên). Cùng cách gọi service dùng chung với authorityLevel/originFlow cố
     * định như createEstablishment (chỉ hỗ trợ R-CV/BANK_LEVEL/Luồng A ở task này — nhánh
     * R-BPTM/GRASSROOTS_LEVEL CHƯA triển khai, tương tự SC-07 grassroots establishment).
     */
    @Operation(summary = "Khởi tạo hồ sơ biến động tổ chức đảng: Giải thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên")
    @RequirePermission(function = "FN1", action = "CREATE")
    @PostMapping("/changes")
    public ResponseEntity<BaseResponse<CaseChangeResponse>> createCaseChange(@RequestBody CaseChangeRequest request) {
        request.validate();
        return BaseResponse.success(caseChangeService.createCaseChange(
                request, EAuthorityLevel.BANK_LEVEL.getId(), Constants.CASE_FLOW_BTCDU));
    }

    /** API-SC08-02 — guard status đúng bước 1 của luồng hồ sơ này (ở service), caseTypeId bất biến. */
    @Operation(summary = "Chỉnh sửa hồ sơ biến động tổ chức đảng: Giải thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên")
    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/change")
    public ResponseEntity<BaseResponse<CaseChangeResponse>> updateCaseChange(@PathVariable String id, @RequestBody CaseChangeRequest request) {
        request.validateForUpdate();
        return BaseResponse.success(caseChangeService.updateCaseChange(id, request));
    }

    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/establishment/board-review")
    public ResponseEntity<BaseResponse<BoardReviewResponse>> updateBoardReview(
            @PathVariable String id, @RequestBody BoardReviewRequest request) {
        request.validate();
        return BaseResponse.success(boardReviewService.upsert(id, request));
    }

    @RequirePermission(function = "FN1", action = "VIEW")
    @GetMapping("/{id}/establishment/board-review")
    public ResponseEntity<BaseResponse<BoardReviewResponse>> getBoardReview(@PathVariable String id) {
        return BaseResponse.success(boardReviewService.get(id));
    }

    /** API-SC05-01 — Bước 3 giai đoạn 1 (trước ban hành), guard status A-08 + role R-CV (ở service). */
    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/establishment/committee-review")
    public ResponseEntity<BaseResponse<CommitteeReviewResponse>> updateCommitteeReview(
            @PathVariable String id, @RequestBody CommitteeReviewRequest request) {
        request.validate();
        return BaseResponse.success(committeeReviewService.upsert(id, request));
    }

    @RequirePermission(function = "FN1", action = "VIEW")
    @GetMapping("/{id}/establishment/committee-review")
    public ResponseEntity<BaseResponse<CommitteeReviewResponse>> getCommitteeReview(@PathVariable String id) {
        return BaseResponse.success(committeeReviewService.get(id));
    }

    /** API-SC05-02 — Bước 3 giai đoạn 2 (sau ban hành), guard status A-15 + role R-CV (ở service). */
    @RequirePermission(function = "FN1", action = "EDIT")
    @PutMapping("/{id}/establishment/decision-documents")
    public ResponseEntity<BaseResponse<DecisionDocumentsResponse>> updateDecisionDocuments(
            @PathVariable String id, @RequestBody DecisionDocumentsRequest request) {
        request.validate();
        return BaseResponse.success(decisionDocumentsService.upsert(id, request));
    }

    /** API-SC06-01 — kích hoạt lưu trữ (sinh 2 văn bản lưu trữ), guard status A-16/B-04 + role R-CV/R-BPTM (ở service). */
    @RequirePermission(function = "FN3", action = "APPROVE")
    @PostMapping("/{id}/archive")
    public ResponseEntity<BaseResponse<ArchiveCaseResponse>> archiveCase(@PathVariable String id) {
        return BaseResponse.success(archiveCaseService.archive(id));
    }

    /** API-SC06-02 — phê duyệt hoàn thành: tạo chính thức PMDV_ORGANIZATION, khóa hồ sơ (ở service). */
    @RequirePermission(function = "FN3", action = "APPROVE")
    @PostMapping("/{id}/complete")
    public ResponseEntity<BaseResponse<CompleteCaseResponse>> completeCase(@PathVariable String id) {
        return BaseResponse.success(caseCompleteService.complete(id));
    }
}
