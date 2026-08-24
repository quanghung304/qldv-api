package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.AttachmentClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseOrganizationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.attachment.UploadAttachmentsRequest;
import com.agribank.qldv_api.response.attachment.AttachmentItemResponse;
import com.agribank.qldv_api.response.attachment.AttachmentUploadResponse;
import com.agribank.qldv_api.storage.S3Service;
import com.agribank.qldv_api.storage.StorageKeyBuilder;
import com.agribank.qldv_api.workflow.WorkflowAssigneeGuard;
import com.agribank.qldvutils.entity.Attachment;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.attachment.AttachmentSummaryResponse;
import com.agribank.qldvutils.response.casemgmt.CaseOrganizationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API-FN5 — đính kèm tài liệu, cơ chế CỘNG DỒN: mỗi lần upload chỉ ghi thêm file mới, KHÔNG đụng
 * tới file đã có (kể cả cùng case_id/workflow_stage). Xóa từng file cụ thể do người dùng tự thực
 * hiện qua {@code DELETE /api/v1/attachments/{id}} (xem {@link #deleteAttachment(String)}), không
 * còn cơ chế "thay thế theo bước" tự động như trước. Tái sử dụng nguyên vẹn {@link S3Service}/
 * {@link StorageKeyBuilder} (task sinh văn bản trước đó) và scope-check của
 * {@link OrganizationService#resolveScope()} (đúng pattern {@code CaseService}/
 * {@code DocumentGenerationService} — KHÔNG viết lại logic phạm vi).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    private static final Set<String> FINAL_STATUS_CODES = Set.of(
            ECaseStatusCode.A_15.getCode(), ECaseStatusCode.B_05.getCode());

    private final CaseClient caseClient;
    private final CaseOrganizationClient caseOrganizationClient;
    private final AttachmentClient attachmentClient;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final S3Service s3Service;
    private final StorageKeyBuilder storageKeyBuilder;
    private final WorkflowAssigneeGuard workflowAssigneeGuard;

    /**
     * Thứ tự BẮT BUỘC: (1) upload toàn bộ file mới lên S3 trước — lỗi giữa chừng thì dọn rác S3,
     * KHÔNG đụng dữ liệu cũ; (2) chỉ sau khi (1) xong hết mới insert các dòng mới xuống qldv-db
     * (saveAll, 1 transaction — đủ atomic vì chỉ ghi 1 bảng, xem AttachmentController qldv-db).
     * KHÔNG xóa/đụng tới attachment đã có — xóa từng file cụ thể do người dùng tự gọi
     * {@code DELETE /api/v1/attachments/{id}}.
     */
    public AttachmentUploadResponse uploadAttachments(String caseId, UploadAttachmentsRequest request) {
        UserDetailsImpl user = requireUser();
        Case caseEntity = requireCaseInScope(caseId);
        requireNotCompleted(caseEntity);
        workflowAssigneeGuard.requireAssignee(caseEntity, user.getId());

        String workflowStage = caseEntity.getStatusId();
        List<MultipartFile> files = request.getFiles();
        List<String> uploadedKeys = new ArrayList<>();
        List<Attachment> newAttachments = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String key = storageKeyBuilder.attachmentKey(caseId, workflowStage,
                        file.getOriginalFilename(), caseEntity.getCreatedAt());
                s3Service.uploadObject(key, readBytes(file), file.getContentType());
                uploadedKeys.add(key);
                newAttachments.add(Attachment.builder()
                        .caseId(caseId)
                        .fileName(file.getOriginalFilename())
                        .filePath(key)
                        .workflowStage(workflowStage)
                        .uploadedBy(user.getId())
                        .uploadedAt(new Timestamp(System.currentTimeMillis()))
                        .build());
            }
        } catch (Exception e) {
            log.error("Upload attachment lỗi giữa chừng cho case {}, dọn rác {} file đã lỡ upload lên S3: {}",
                    caseId, uploadedKeys.size(), e.getMessage(), e);
            for (String key : uploadedKeys) {
                safeDeleteObject(key);
            }
            throw new CommonException("Upload tài liệu đính kèm thất bại, vui lòng thử lại: " + e.getMessage());
        }

        List<Attachment> saved = attachmentClient.saveAll(newAttachments).getData();

        List<AttachmentItemResponse> savedFiles = saved.stream()
                .map(a -> new AttachmentItemResponse(a.getId(), a.getFileName()))
                .toList();
        return new AttachmentUploadResponse(savedFiles);
    }

    public List<AttachmentSummaryResponse> listAttachments(String caseId, String workflowStage) {
        requireCaseInScope(caseId);
        return safeList(attachmentClient.findSummaryByCaseId(caseId, workflowStage).getData());
    }

    public record AttachmentDownload(byte[] content, String fileName) {
    }

    public AttachmentDownload downloadAttachment(String id) {
        Attachment attachment = requireAttachment(id);
        verifyCaseAccess(attachment.getCaseId());
        byte[] content = s3Service.downloadObject(attachment.getFilePath());
        return new AttachmentDownload(content, attachment.getFileName());
    }

    public void deleteAttachment(String id) {
        UserDetailsImpl user = requireUser();
        Attachment attachment = requireAttachment(id);
        Case caseEntity = requireCaseInScope(attachment.getCaseId());
        requireNotCompleted(caseEntity);
        workflowAssigneeGuard.requireAssignee(caseEntity, user.getId());

        s3Service.deleteObject(attachment.getFilePath());
        attachmentClient.deleteById(id);
    }

    // ---------------------------------------------------------------- helpers

    private Attachment requireAttachment(String id) {
        return attachmentClient.findById(id).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài liệu đính kèm"));
    }

    private Case requireCaseInScope(String caseId) {
        verifyCaseAccess(caseId);
        return caseClient.findById(caseId).getData()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ"));
    }

    /**
     * "Hoàn thành/khóa" xác định qua status_id thuộc 2 trạng thái cuối A-15/B-05 (WorkflowEngine
     * cập nhật status_id ở MỌI transition, tín hiệu real-time đáng tin cậy) — KHÔNG dùng
     * Case.completedAt hay Attachment.is_locked vì 2 field đó hiện CHƯA có bất kỳ luồng ghi nào
     * gán giá trị (xem báo cáo cuối task).
     */
    private void requireNotCompleted(Case caseEntity) {
        if (FINAL_STATUS_CODES.contains(caseEntity.getStatusId())) {
            throw new ForbiddenException("Hồ sơ đã hoàn thành, không thể thêm/xóa tài liệu đính kèm");
        }
    }

    private void verifyCaseAccess(String caseId) {
        OrganizationScope scope = organizationService.resolveScope();
        if (scope.isFull()) {
            return;
        }
        List<CaseOrganizationSummaryResponse> orgLinks = safeList(
                caseOrganizationClient.findWithOrganizationByCaseId(caseId).getData());
        boolean allowed = orgLinks.stream().anyMatch(link -> scope.isAllowed(link.getOrganizationId()));
        if (!allowed) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền truy cập hồ sơ này");
        }
    }

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private void safeDeleteObject(String key) {
        try {
            s3Service.deleteObject(key);
        } catch (Exception e) {
            log.error("Không xóa được object S3 (key={}): {}", key, e.getMessage(), e);
        }
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new CommonException("Không đọc được nội dung file upload: " + file.getOriginalFilename());
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
