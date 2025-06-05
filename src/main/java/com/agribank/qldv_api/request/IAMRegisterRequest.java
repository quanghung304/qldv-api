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
    private String password;
    private List<Integer> roleIds;
    private List<Integer> applicationIds;
    private String vneid;
    private String phone;
    private Integer staffCode;
    private String fullName;
    private String address;
    private String jobPosition;
    private Integer depId;
    private String userKind;
}
