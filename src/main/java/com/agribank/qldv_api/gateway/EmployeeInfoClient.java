package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "employeeInfoClient",  url = "${qldv.database.url}" + "/api/v1/emp-info", configuration = DatabaseFeignConfiguration.class)
public interface EmployeeInfoClient {

    @GetMapping("/{empno}")
    BaseResponse<EmployeeInfoDto> findByEmpno(@PathVariable String empno);

    @PostMapping("/batch")
    DefaultListResponse<EmployeeInfoDto> findByEmpnos(@RequestBody List<String> empnos);
}
