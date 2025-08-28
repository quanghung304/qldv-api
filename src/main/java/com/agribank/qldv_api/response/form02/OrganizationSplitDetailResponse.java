package com.agribank.qldv_api.response.form02;

import com.agribank.qldv_api.response.BaseFormDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrganizationSplitDetailResponse extends BaseFormDto {
    String id;
    String organizationCodeSplit;
    String organizationNameSplit;
    List<NewOrganizationResponse> splitDetailsNew;
    List<OldOrganizationResponse> splitDetailsOld;

    @Data
    public static class NewOrganizationResponse {
        String code;
        String name;
        String form;
        List<DV> members;
    }

    @Data
    public static class OldOrganizationResponse {
        Organization oldOrganization;
        List<DV> members;
    }
}