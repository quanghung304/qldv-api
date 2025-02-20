package com.agribank.qldv_api.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class IAMRegisterRequest {
    private String username;
    @NotNull(message = "BRCD is required")
    private Integer brcd;
    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email is required")
    private String email;
    private String password;
    private List<Integer> roleIds = new ArrayList<Integer>();
    @NotEmpty(message = "application ids is required")
    private List<Integer> applicationIds = new ArrayList<Integer>();
    private Integer vneid;
    private String phone;
    private Integer staffCode;
    private String fullName;
    private String address;
    private String jobPosition;
    private Integer depId;
    private String userKind;
}
