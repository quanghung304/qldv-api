package com.agribank.qldv_api.controller;


import com.agribank.qldv_api.request.dv.DVRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.dv.DVResponse;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldv_api.service.DVService;
import com.agribank.qldvutils.entity.DvDraft;
import com.agribank.qldvutils.request.SearchDVRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dv")
public class DVController {
    private final DVService service;

    @PostMapping("/search")
    public ResponseEntity<DefaultResponse<PageResponse<DVResponse>>> search(@RequestBody SearchDVRequest request) {
        return DefaultResponse.success(service.search(request));
    }

    @GetMapping("/organization")
    public ResponseEntity<DefaultResponse<List<DVResponse>>> getDVByOrganization(@RequestParam(name = "organization", required = false) String organization) {
        return DefaultResponse.success(service.getDVByOrganization(organization));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<DvDraft>> createDVRequest(@RequestBody DVRequest request)  {
        return DefaultResponse.success("Tạo yêu cầu thêm mới đảng viên thành công", service.create(request));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<DvDraft>> updateDVRequest(@RequestBody DVRequest request) {
        return DefaultResponse.success("Tạo yêu cầu cập nhật đảng viên thành công", service.update(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<DV>> getDVDetail(@PathVariable String id) {
        return DefaultResponse.success(service.findById(id));
    }
}
