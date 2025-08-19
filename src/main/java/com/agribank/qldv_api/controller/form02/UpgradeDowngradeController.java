package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.OrganizationUpDownRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.form02.OrganizationUpDownService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDown;
import com.agribank.qldvutils.entity.form02.updown.OrganizationUpDownDraft;
import com.agribank.qldvutils.request.form02.UpdownOrganizationFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class UpgradeDowngradeController {
    private final OrganizationUpDownService updownService;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("create")
    public ResponseEntity<DefaultResponse<OrganizationUpDownDraft>> createTransformRequest(@RequestBody @Valid OrganizationUpDownRequest request) {
        request.validate();
        return DefaultResponse.success(updownService.createTransformRequest(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultResponse<PageResponse<OrganizationUpDown>>> getList(@RequestBody UpdownOrganizationFilterRequest request) {
        return DefaultResponse.success(updownService.getList(request));
    }

    @GetMapping("{id}")
    public ResponseEntity<DefaultResponse<OrganizationUpDown>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(updownService.getDetail(id));
    }

    @PutMapping("update")
    public ResponseEntity<DefaultResponse<Request>> update(@RequestBody @Valid OrganizationUpDownRequest request) {
        return DefaultResponse.success(updownService.update(request));
    }

    @GetMapping("draft")
    public ResponseEntity<DefaultResponse<OrganizationUpDownDraft>> getDraft(@RequestParam String id) {
        return DefaultResponse.success(updownService.getDraft(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("draft/update")
    public ResponseEntity<DefaultResponse<OrganizationUpDownDraft>> updateDraft(@RequestBody @Valid OrganizationUpDownRequest request) {
        return DefaultResponse.success(updownService.updateDraft(request));
    }
}
