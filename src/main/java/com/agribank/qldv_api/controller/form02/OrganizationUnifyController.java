package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.MergeOrganizationRequest;
import com.agribank.qldv_api.request.form02.UnifyOrganizationRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.form02.OrganizationMergeService;
import com.agribank.qldv_api.service.form02.OrganizationUnifyService;
import com.agribank.qldvutils.entity.Request;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
