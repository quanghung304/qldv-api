package com.agribank.qldv_api.controller.report26;

import com.agribank.qldv_api.request.leave_party.LeavePartyRequest;
import com.agribank.qldv_api.service.report26.LeavePartyService;
import com.agribank.qldvutils.entity.report26.LeavePartyDraft;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/leave-party")
public class LeavePartyController {
    private final LeavePartyService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<LeavePartyDraft>> create(@RequestBody LeavePartyRequest request) {
        request.validate();
        return BaseResponse.success(service.createDraft(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createRequestDelete(id), null);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResponse<RP26DetailResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<BaseResponse<LeavePartyDraft>> getDraftDetail(@PathVariable String id) {
        return BaseResponse.success(service.getDraftDetail(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft")
    public ResponseEntity<BaseResponse<String>> updateDraft(@RequestBody LeavePartyRequest request) {
        return BaseResponse.success(service.updateDraft(request));
    }
}
