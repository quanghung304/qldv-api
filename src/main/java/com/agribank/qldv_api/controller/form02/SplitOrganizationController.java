package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.SplitOrganizationRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.ApiLogResponse;
import com.agribank.qldv_api.service.form02.SplitOrganizationService;
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
    private final SplitOrganizationService spitOrganizationService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<PageResponse<ApiLogResponse>>> search(@RequestBody @Valid SplitOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(spitOrganizationService.createSplitRequest(request), null);
    }
}
