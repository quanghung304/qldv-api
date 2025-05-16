package com.agribank.qldv_api.response.membershipProposal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipProposalResponse {
    String id;
    String organizationCode;
    String staffCode;
    String reason;
    //Số kết luận nghị quyết
    String resolutionNumber;
    //Ngày kết luận nghị quyết
    Date resolutionDate;
    //Số quyết định
    String decisionNumber;
    //Ngày QĐ
    Date decisionDate;
    Integer status;
}
