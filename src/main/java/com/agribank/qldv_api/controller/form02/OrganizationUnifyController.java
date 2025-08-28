package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.UnifyOrgUpdateRequest;
import com.agribank.qldv_api.request.form02.UnifyOrganizationRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.form02.OrganizationMerResponse;
import com.agribank.qldv_api.response.form02.OrganizationMergeResponse;
import com.agribank.qldv_api.response.form02.OrganizationUnifyResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.form02.OrganizationUnifyService;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.request.form02.SearchOrganizationUnionRequest;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/unify")
@RequiredArgsConstructor
public class OrganizationUnifyController {
    private final OrganizationUnifyService unifyService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<Request>> createUnifyRequest(@RequestBody @Valid UnifyOrganizationRequest request) {
        request.validate();
        return DefaultResponse.success(unifyService.createUnifyRequest(request));
    }

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<OrganizationMerResponse>>> search(@RequestBody @Valid SearchOrganizationUnionRequest request) {
        return DefaultResponse.success(unifyService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<OrganizationMergeResponse>> search(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(unifyService.getDetail(id));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<DefaultResponse<OrganizationUnifyResponse>> getDraftDetail(@PathVariable(name = "id") String id) {
        return DefaultResponse.success(unifyService.getDraftDetail(id));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<RequestResponse>> updateUnifyRequest(@RequestBody @Valid UnifyOrgUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(unifyService.update(request));
    }

    @PutMapping("/update-draft")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<String>> updateDraft(@RequestBody @Valid UnifyOrgUpdateRequest request) {
        request.validate();
        return DefaultResponse.success(unifyService.updateDraft(request));
    }
}
