package com.agribank.qldv_api.response.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentUploadResponse {
    private List<AttachmentItemResponse> savedFiles;
}
