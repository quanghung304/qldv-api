package com.agribank.qldv_api.response.organization;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationHierarchyResponse {
    String code;
    String name;
    String form;
    String parentCode;
    List<OrganizationHierarchyResponse> childs;
}
