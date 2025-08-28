package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.SplitOrganizationRequest;
import com.agribank.qldv_api.request.form02.SplitOrganizationUpdateRequest;
import com.agribank.qldv_api.request.form02.UnifyOrgUpdateRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.ApiLogResponse;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.response.form02.OrganizationSplitDetailResponse;
import com.agribank.qldv_api.response.form02.OrganizationSplitResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.form02.SplitOrganizationService;
import com.agribank.qldvutils.request.form02.SearchOrganizationSplitRequest;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/split")
@RequiredArgsConstructor
public class SplitOrganizationController {
    private final SplitOrganizationService splitOrganizationService;

    @PostMapping("create")
    //@PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<PageResponse<ApiLogResponse>>> create(@RequestBody @Valid SplitOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(splitOrganizationService.createSplitRequest(request), null);
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<OrganizationSplitResponse>>> search(@RequestBody @Valid SearchOrganizationSplitRequest request) {
        return DefaultResponse.success(splitOrganizationService.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<OrganizationSplitDetailResponse>> getDetail(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(splitOrganizationService.getDetail(id));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<DefaultResponse<OrganizationSplitDetailResponse>> getDraftDetail(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(splitOrganizationService.getDraftDetail(id));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<RequestResponse>> update(@RequestBody @Valid SplitOrganizationUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(splitOrganizationService.update(request));
    }

    @PutMapping("/update-draft")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<String>> updateDraft(@RequestBody @Valid SplitOrganizationUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(splitOrganizationService.updateDraft(request));
    }
}
