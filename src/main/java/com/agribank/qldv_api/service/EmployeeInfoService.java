package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.EmployeeInfoClient;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeeInfoService {
    private final EmployeeInfoClient employeeInfoClient;

    public EmployeeInfoDto findByEmpno(String empno) {
        EmployeeInfoDto employeeInfo = employeeInfoClient.findByEmpno(empno).getData();

        return employeeInfo;
    }
}
