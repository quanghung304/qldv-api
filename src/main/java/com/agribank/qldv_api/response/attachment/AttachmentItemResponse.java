package com.agribank.qldv_api.response.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentItemResponse {
    private String id;
    private String fileName;
}
