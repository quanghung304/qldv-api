package com.agribank.qldv_api.response.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** replacedCount: số tài liệu CŨ (cùng case_id + workflow_stage) đã bị thay thế — để FE hiển thị rõ đây là REPLACE, không phải cộng dồn. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentUploadResponse {
    private List<AttachmentItemResponse> savedFiles;
    private int replacedCount;
}
