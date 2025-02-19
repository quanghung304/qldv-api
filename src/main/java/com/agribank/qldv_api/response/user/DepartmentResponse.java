package com.agribank.qldv_api.response.user;

import lombok.Data;

@Data
public class DepartmentResponse {
    private Integer id;
    private String deptnm;
    private String deptcd;
    private Integer brcd;
}
