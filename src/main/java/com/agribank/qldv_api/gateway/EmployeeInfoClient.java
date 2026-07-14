package com.agribank.qldv_api.gateway;

import com.agribank.qldv_api.gateway.config.DatabaseFeignConfiguration;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employeeInfoClient",  url = "${qldv.database.url}" + "/api/v1/emp-info", configuration = DatabaseFeignConfiguration.class)
public interface EmployeeInfoClient {

    @GetMapping("/{empno}")
    BaseResponse<EmployeeInfoDto> findByEmpno(@PathVariable String empno);
}
