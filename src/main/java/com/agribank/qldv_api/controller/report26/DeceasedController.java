package com.agribank.qldv_api.controller.report26;

import com.agribank.qldv_api.request.deceased.DeceasedRequest;
import com.agribank.qldv_api.response.report26.DeceasedResponse;
import com.agribank.qldv_api.service.report26.DeceasedService;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.report26.RP26DetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/deceased")
public class DeceasedController {
    private final DeceasedService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<DeceasedDraft>> create(@RequestBody DeceasedRequest request) {
        request.validate();
        return BaseResponse.success(service.createDraft(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/create-request-delete/{id}")
    public ResponseEntity<BaseResponse<String>> create(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createRequestDelete(id), null);
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<BaseResponse<RP26DetailResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }

    @GetMapping("/draft/detail")
    public ResponseEntity<BaseResponse<DeceasedResponse>> getDraftDetail(@RequestParam(name = "id") String id) {
        return BaseResponse.success(service.getDraftDetail(id));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/draft/update")
    public ResponseEntity<BaseResponse<DeceasedResponse>> updateDraft(@RequestBody DeceasedRequest request) {
        request.validate();
        return BaseResponse.success(service.updateDraft(request));
    }
}
