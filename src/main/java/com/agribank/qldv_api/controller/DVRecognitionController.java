package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.dvRecognition.DVRecognitionRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.dvRecognition.DVRecognitionResponse;
import com.agribank.qldv_api.service.DVRecognitionService;
import com.agribank.qldvutils.request.SearchDVRecognitionRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.agribank.qldv_api.response.DefaultResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/dv-recognition", produces = "application/json")
public class DVRecognitionController {
    private final DVRecognitionService service;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<DVRecognitionResponse>>> search(@RequestBody SearchDVRecognitionRequest request) {
        request.validate();
        return BaseResponse.success(service.search(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @PostMapping("/create-or-update")
    public ResponseEntity<DefaultResponse<String>> addDVRecognition(@RequestBody DVRecognitionRequest request) {
        return success(service.createOrUpdateDraft(request));
    }

    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<String>> delete(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.createRequestDelete(id), null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DVRecognitionResponse>> getDetail(@PathVariable(name = "id") String id) {
        return BaseResponse.success(service.getDetail(id));
    }
}
