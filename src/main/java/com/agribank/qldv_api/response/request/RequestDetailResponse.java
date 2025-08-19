package com.agribank.qldv_api.response.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDetailResponse {
    String id;
    Integer type;
    String formName;
    Integer status;
    String creator;
    String approver;
    String deniedReason;
    Object oldData;
    Object newData;
    List<ChangedData> changedData;
    String referenceId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangedData {
        String name;
        String oldData;
        String newData;
    }
}
