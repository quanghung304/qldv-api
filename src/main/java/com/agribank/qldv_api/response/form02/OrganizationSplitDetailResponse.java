package com.agribank.qldv_api.response.form02;

import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.entity.DV;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationSplitDetailResponse extends BaseFormDto {
    String id;
    String organizationCodeSplit;
    String organizationNameSplit;
    List<NewOrganizationResponse> splitDetails;

    @Data
    public static class NewOrganizationResponse {
        String code;
        String name;
        String form;
        List<DV> members;
    }
}