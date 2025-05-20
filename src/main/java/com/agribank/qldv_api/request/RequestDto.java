package com.agribank.qldv_api.request;

import lombok.Data;

@Data
public class RequestDto {
    private String id;
    private String formId;
    private String formName;
    private String action;
    private String referenceId;
    private String oldData;
    private String newData;
    private String createdBy;
    private String approvedBy;
    private Integer status;
}
