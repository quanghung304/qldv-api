package com.agribank.qldv_api.response.form02;

import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.dto.OrganizationDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationMergeResponse extends BaseFormDto {
    String id;
    String organizationCode;
    String organizationName;
    String form;
    String decisionCommittee;
    OrganizationDto decision;
    List<MergeDetailResponse> mergeDetails;

    @Data
    public static class MergeDetailResponse {
        String code;
        String name;
    }
}
