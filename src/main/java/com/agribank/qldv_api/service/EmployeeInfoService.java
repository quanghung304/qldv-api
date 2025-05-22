package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.EmployeeInfoClient;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeInfoService {
    private final EmployeeInfoClient employeeInfoClient;

    public EmployeeInfoDto findByEmpno(String empno) {
        return employeeInfoClient.findByEmpno(empno).getData();
    }
}
