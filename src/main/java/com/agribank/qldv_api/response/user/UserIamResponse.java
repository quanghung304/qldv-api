package com.agribank.qldv_api.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIamResponse {
    private Integer id;
    private Integer brcd;
    private String username;
    private String email;
    private Integer vneid;
    private String phone;
    private Integer staffCode;
    private String fullName;
    private String address;
    private Integer gender;
    private String jobPosition;
    private Integer changedPassword;
    private Integer active;
    private String userCreated;
    private String userKind;
    private DepartmentResponse department;
}