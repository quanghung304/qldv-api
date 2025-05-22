package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.service.EmployeeInfoService;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/emp-info")
public class EmployeeInfoController {
    private final EmployeeInfoService employeeInfoService;

    @GetMapping("/{empno}")
    public ResponseEntity<DefaultResponse<EmployeeInfoDto>> findByEmpno(@PathVariable String empno) {
        return DefaultResponse.success(employeeInfoService.findByEmpno(empno));
    }
}
