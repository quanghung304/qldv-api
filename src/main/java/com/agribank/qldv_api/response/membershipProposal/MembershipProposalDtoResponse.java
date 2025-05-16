package com.agribank.qldv_api.response.membershipProposal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipProposalDtoResponse extends MembershipProposalResponse{
    String vneid;
    Integer userBrcdAccepted;
    String usernameAccepted;
    String usernameCreated;
    String userBrcdCreated;
    String organizationCode;
}
