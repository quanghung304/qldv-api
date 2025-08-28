package com.agribank.qldv_api.response.form02;

import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationUnifyResponse extends BaseFormDto {
    String id;
    String organizationCode;
    String organizationName;
    String form;
    String decisionCommittee;
    List<UnifyDetailResponse> unifyDetails;

    @Data
    public static class UnifyDetailResponse {
        Organization organization;
        List<DV> members;
    }
}
