package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.request.organizationTransform.OrganizationTransformRequest;
import com.agribank.qldv_api.response.DefaultListResponse;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.TransformationHistoryService;
import com.agribank.qldvutils.entity.TransformationHistoryDraft;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class TransformationHistoryController {
    private final TransformationHistoryService historyService;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("create")
    public ResponseEntity<DefaultResponse<TransformationHistoryDraft>> createTransformRequest(@RequestBody @Valid OrganizationTransformRequest request) {
        return DefaultResponse.success(historyService.createTransformRequest(request));
    }

    @GetMapping("get-draft")
    public ResponseEntity<DefaultListResponse<TransformationHistoryDraft>> getDraftList(@RequestParam Integer status) {
        return DefaultListResponse.success(historyService.getDrafttList(status));
    }

    @GetMapping("get/{id}")
    public ResponseEntity<DefaultResponse<TransformationHistoryDraft>> getDraft(@PathVariable String id) {
        return DefaultResponse.success(historyService.getDraft(id));
    }

    @PreAuthorize("hasAuthority('QLDV_APPROVER')")
    @PutMapping("update")
    public ResponseEntity<DefaultResponse<Object>> update(@RequestBody List<ApproveRequest> requestList) {
        return DefaultResponse.success(historyService.update(requestList));
    }
}
