package com.agribank.qldv_api.controller.form02;

import com.agribank.qldv_api.request.form02.DissolveDisbandRequest;
import com.agribank.qldv_api.service.DissolveDisbandDraftService;
import com.agribank.qldvutils.entity.form02.dissolve.DissolveDisbandDraft;
import com.agribank.qldvutils.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/establishment-dissolve-draft", produces = "application/json")
@RequiredArgsConstructor
public class DissolveDisbandDraftController {
    private final DissolveDisbandDraftService service;

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create-or-update")
    public ResponseEntity<BaseResponse<DissolveDisbandDraft>> createOrUpdate(@RequestBody @Valid DissolveDisbandRequest request) {
        request.validate();
        return BaseResponse.success(service.createOrUpdate(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.delete(id), null);
    }


    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<DissolveDisbandDraft>> update(@RequestBody @Valid DissolveDisbandRequest request) {
        request.validate();
        return BaseResponse.success(service.update(request));
    }
}
