package com.agribank.qldv_api.controller.form02;
import com.agribank.qldv_api.request.form02.OrganizationRenameRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.form02.OrganizationRenameResponse;
import com.agribank.qldv_api.service.form02.OrganizationRenameService;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRename;
import com.agribank.qldvutils.entity.form02.rename.OrganizationRenameDraft;
import com.agribank.qldvutils.request.form02.OrganizationRenameFilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/rename")
@RequiredArgsConstructor
public class OrganizationRenameController {
    private final OrganizationRenameService renameService;

    @PostMapping("create")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<OrganizationRenameDraft>> createRenameRequest(@RequestBody @Valid OrganizationRenameRequest request) {
        return DefaultResponse.success(renameService.createRenameRequest(request));
    }

    @PostMapping("list")
    public ResponseEntity<DefaultResponse<PageResponse<OrganizationRename>>> getList(@RequestBody OrganizationRenameFilterRequest request) {
        return DefaultResponse.success(renameService.getList(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultResponse<OrganizationRename>> getDetail(@PathVariable String id) {
        return DefaultResponse.success(renameService.getDetail(id));
    }

    @PutMapping("update")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<OrganizationRenameDraft>> updateMergeRequest(@RequestBody @Valid OrganizationRenameRequest request) {
        return DefaultResponse.success(renameService.updateRenameRequest(request));
    }

    @GetMapping("/draft")
    public ResponseEntity<DefaultResponse<OrganizationRenameResponse>> getDraftDetail(@RequestParam(name = "id") String id) {
        return DefaultResponse.success(renameService.getDraftDetail(id));
    }

    @PutMapping("/update/draft")
    @PreAuthorize("hasAuthority('QLDV_TELLER')")
    public ResponseEntity<DefaultResponse<OrganizationRenameResponse>> updateDraftRequest(@RequestBody @Valid OrganizationRenameRequest request) {
        return DefaultResponse.success(renameService.updateDraft(request));
    }
}
