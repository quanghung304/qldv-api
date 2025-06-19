package com.agribank.qldv_api.response.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {
    String id;
    Integer type;
    String formCode;
    String formName;
    String organizationCode;
    String organizationName;
    String staffName;
    Object oldData;
    Object newData;
    Date createdAt;
    String creator;
    Date approvedAt;
    String approver;
    Integer status;
    String deniedReason;
}
