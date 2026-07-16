package com.agribank.qldv_api.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IAMRegisterRequest {
    private String username;
    private Integer brcd;
    private String email;
    private List<Integer> roleIds;
    private List<Integer> applicationIds;
    private String fullName;
    private Integer depId;
    private Integer staffCode;
}
