package com.agribank.qldv_api.controller.report26;

import com.agribank.qldv_api.request.party_activity_exemption.PartyActivityExemptionRequest;
import com.agribank.qldv_api.service.report26.PartyActivityExemptionService;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/party-activity-exemption")
public class PartyActivityExemptionController {
    private final PartyActivityExemptionService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<PartyActivityExemptionDraft>> create(@RequestBody PartyActivityExemptionRequest request) {
        request.validate();
        return BaseResponse.success(service.createDraft(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createRequestDelete(id), null);
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResponse<RP26DetailResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }
}
